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
    
    # LaTeX escape sequence errors  
    r'(?<!r)["\'].*?(?<!\\)\\[a-zA-Z].*?["\']':
        "Possible invalid LaTeX escape. Use raw strings: r'\\alpha' instead of '\\alpha'. Check all strings containing backslashes.",
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

    return {
        "valid": len(errors) == 0,
        "errors": errors,
        "warnings": warnings,
        "fixed_code": fixed_code
    }
