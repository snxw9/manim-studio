import re
import ast

# Methods that don't exist in Manim — map to fix suggestions
BANNED_PATTERNS = {
    r'\.intersection\(': "Use line_intersection([l1.get_start(), l1.get_end()], [l2.get_start(), l2.get_end()]) from manim.utils.space_ops",
    r'\.midpoint\(': "Use .get_center() instead",
    r'\.length\(': "Use .get_length() instead",
    r'\.rotate_about\(': "Use .rotate(angle, about_point=point) instead",
    r'\.intersects\(': "No direct method — compute manually with line_intersection()",
    r'\.get_intersection\(': "Use line_intersection() from manim.utils.space_ops",
    r'\.set_font_size\(': "Pass font_size= in the constructor instead",
    
    # Sector constructor errors
    r'Sector\s*\([^)]*outer_radius': 
        "Sector does not accept outer_radius. Use Sector(radius=..., angle=...) OR use AnnularSector(inner_radius=..., outer_radius=..., angle=...)",
    r'Sector\s*\([^)]*inner_radius': 
        "Sector does not accept inner_radius. Use AnnularSector(inner_radius=..., outer_radius=...) for ring shapes",

    # Camera frame in wrong scene class
    r'self\.camera\.frame':
        "self.camera.frame requires MovingCameraScene not Scene. "
        "Change class MyScene(Scene) to class MyScene(MovingCameraScene)",
    
    # 3D objects in wrong scene
    r'(?:Sphere|Cylinder|Cone|Cube|Prism)\s*\(':
        "3D objects require ThreeDScene. "
        "Change class MyScene(Scene) to class MyScene(ThreeDScene)",
    
    r'set_camera_orientation\s*\(':
        "set_camera_orientation requires ThreeDScene not Scene",
    
    r'begin_ambient_camera_rotation\s*\(':
        "begin_ambient_camera_rotation requires ThreeDScene not Scene",
    
    # Performance traps
    r'for\s+\w+\s+in\s+range\s*\(\s*(?:3[6-9]\d|[4-9]\d{2}|\d{4,})':
        "Loop with 360+ iterations detected. "
        "Use TracedPath or ParametricFunction instead of looping to create paths. "
        "Never call self.wait() or self.add() inside large loops.",

    # Removed in Manim v0.17+ — replaced with Surface
    r'ParametricSurface\s*\(':
        "ParametricSurface was removed in Manim v0.17. "
        "Use Surface() instead:\n"
        "Surface(func, u_range=[a,b], v_range=[a,b], resolution=(20,20))\n"
        "where func takes (u,v) and returns np.array([x, y, z])",

    # Other commonly hallucinated removed/renamed classes
    r'GraphScene\s*\(':
        "GraphScene was removed. Use Scene with Axes() instead:\n"
        "axes = Axes(x_range=[a,b], y_range=[a,b])\n"
        "curve = axes.plot(lambda x: f(x), color=BLUE)",

    r'NumberLine\s*\(.*unit_size':
        "unit_size parameter was removed from NumberLine. "
        "Use length= and x_range= to control size instead.",

    r'ShowCreation\s*\(':
        "ShowCreation was renamed to Create in Manim v0.7+. "
        "Use Create() instead.",

    r'FadeInFrom\s*\(':
        "FadeInFrom was removed. Use FadeIn(obj, shift=direction) instead.",

    r'FadeOutAndShift\s*\(':
        "FadeOutAndShift was removed. Use FadeOut(obj, shift=direction) instead.",

    r'CurvedDoubleArrow\s*\(':
        "CurvedDoubleArrow does not exist. Use CurvedArrow() instead.",

    r'SurroundingRectangle\s*\([^)]*buff\s*=\s*(?![0-9])':
        "SurroundingRectangle buff= must be a number like buff=0.1",

    r'\.to_corner\s*\([^)]*buff':
        "to_corner() does not accept buff as positional arg. "
        "Use to_corner(UL, buff=0.2) with keyword argument.",
}

def fix_latex_escapes(code: str) -> str:
    """
    Auto-fix common LaTeX escape issues in generated Manim code.
    Converts Tex("...") and MathTex("...") to use raw strings when backslashes detected.
    """
    def make_raw(match):
        full = match.group(0)
        func = match.group(1)   # Tex or MathTex
        quote = match.group(2)  # " or '
        content = match.group(3)
        
        # If already a raw string, skip
        if full.startswith('r'):
            return full
        
        # If content has backslashes that aren't doubled, make it raw
        if re.search(r'(?<!\\)\\[a-zA-Z]', content):
            # Remove any existing double-escapes to normalise, then wrap as raw
            content_fixed = content.replace('\\\\', '\\')
            return f'{func}(r{quote}{content_fixed}{quote})'
        return full
    
    # Match Tex("...") and MathTex("...") — single or double quoted
    pattern = r'((?:Math)?Tex)\((["\']{1})((?:(?!\2).)*)\2\)'
    code = re.sub(pattern, make_raw, code)
    return code

def validate_scene_class_consistency(code: str) -> list[str]:
    """Check that scene class matches the features used."""
    errors = []
    
    uses_camera_frame = bool(re.search(r'self\.camera\.frame', code))
    uses_3d = bool(re.search(r'(?:Sphere|Cylinder|Cone|Cube|Prism|set_camera_orientation|begin_ambient)', code))
    
    class_match = re.search(r'class\s+\w+\s*\(\s*(\w+)\s*\)', code)
    base_class = class_match.group(1) if class_match else "Scene"
    
    if uses_camera_frame and base_class == "Scene":
        errors.append(
            f"Class inherits from Scene but uses self.camera.frame. "
            f"Change to MovingCameraScene."
        )
    
    if uses_3d and base_class == "Scene":
        errors.append(
            f"Class inherits from Scene but uses 3D features. "
            f"Change to ThreeDScene."
        )
    
    return errors

def validate_performance(code: str) -> list[str]:
    """Check for performance traps."""
    warnings = []
    
    # Check for large loops with self operations inside
    loop_pattern = re.finditer(
        r'for\s+\w+\s+in\s+range\s*\((\d+)', code
    )
    for match in loop_pattern:
        count = int(match.group(1))
        if count > 50:
            # Check if loop body has self.wait or self.add
            loop_start = match.start()
            loop_body = code[loop_start:loop_start + 300]
            if 'self.wait' in loop_body or 'self.add' in loop_body:
                warnings.append(
                    f"Performance trap: loop of {count} iterations with "
                    f"self.wait() or self.add() will take very long to render. "
                    f"Use TracedPath, ParametricFunction, or always_redraw instead."
                )
    
    return warnings

def validate_manim_code(code: str) -> dict:
    """
    Returns { valid: bool, errors: list[str], warnings: list[str], fixed_code: str }
    """
    errors = []
    warnings = []
    fixed_code = code

    # Auto-fix LaTeX escapes first
    fixed_code = fix_latex_escapes(fixed_code)

    # Check for banned method patterns
    for pattern, suggestion in BANNED_PATTERNS.items():
        matches = re.findall(pattern, code)
        if matches:
            errors.append(f"Invalid Manim method '{pattern.strip(r'\\.')}': {suggestion}")

    # Check for valid Python syntax
    try:
        ast.parse(fixed_code)
    except SyntaxError as e:
        errors.append(f"Python syntax error at line {e.lineno}: {e.msg}")

    # Check Scene class exists
    if not re.search(r'class\s+\w+\s*\(\s*(Scene|MovingCameraScene|ThreeDScene)\s*\)', fixed_code):
        errors.append("No valid Scene class found. Must inherit from Scene, MovingCameraScene, or ThreeDScene.")

    # Check construct method exists
    if 'def construct(self)' not in fixed_code:
        errors.append("Missing construct(self) method.")

    # Check manim is imported
    if 'from manim import' not in fixed_code and 'import manim' not in fixed_code:
        warnings.append("Missing manim import — adding automatically.")
        fixed_code = "from manim import *\n" + fixed_code

    # New checks
    errors.extend(validate_scene_class_consistency(fixed_code))
    warnings.extend(validate_performance(fixed_code))

    # Auto-fix: add MovingCameraScene if camera.frame used with Scene
    if (re.search(r'self\.camera\.frame', fixed_code) and 
        re.search(r'class\s+\w+\s*\(\s*Scene\s*\)', fixed_code)):
        fixed_code = re.sub(
            r'(class\s+\w+\s*\()\s*Scene\s*\)',
            r'\1MovingCameraScene)',
            fixed_code
        )
        warnings.append("Auto-fixed: Changed Scene to MovingCameraScene")

    # Auto-fix ParametricSurface -> Surface
    if 'ParametricSurface' in fixed_code:
        fixed_code = fixed_code.replace('ParametricSurface', 'Surface')
        warnings.append(
            "Auto-fixed: ParametricSurface replaced with Surface "
            "(ParametricSurface was removed in Manim v0.17)"
        )

    return {
        "valid": len(errors) == 0,
        "errors": errors,
        "warnings": warnings,
        "fixed_code": fixed_code
    }
