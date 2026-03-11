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
}

def validate_manim_code(code: str) -> dict:
    """
    Returns { valid: bool, errors: list[str], warnings: list[str], fixed_code: str }
    """
    errors = []
    warnings = []
    fixed_code = code

    # Check for banned method patterns
    for pattern, suggestion in BANNED_PATTERNS.items():
        matches = re.findall(pattern, code)
        if matches:
            errors.append(f"Invalid Manim method '{pattern.strip(r'\\.')}': {suggestion}")

    # Check for valid Python syntax
    try:
        ast.parse(code)
    except SyntaxError as e:
        errors.append(f"Python syntax error at line {e.lineno}: {e.msg}")

    # Check Scene class exists
    if not re.search(r'class\s+\w+\s*\(\s*(Scene|MovingCameraScene|ThreeDScene)\s*\)', code):
        errors.append("No valid Scene class found. Must inherit from Scene, MovingCameraScene, or ThreeDScene.")

    # Check construct method exists
    if 'def construct(self)' not in code:
        errors.append("Missing construct(self) method.")

    # Check manim is imported
    if 'from manim import' not in code and 'import manim' not in code:
        warnings.append("Missing manim import — adding automatically.")
        fixed_code = "from manim import *\n" + fixed_code

    return {
        "valid": len(errors) == 0,
        "errors": errors,
        "warnings": warnings,
        "fixed_code": fixed_code
    }
