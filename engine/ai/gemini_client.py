import os
import google.generativeai as genai
from .prompt_builder import get_system_prompt
from renderer.code_validator import validate_manim_code

async def generate_manim_code(user_prompt: str, template: str = "none", max_retries: int = 3) -> str:
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        raise ValueError("GEMINI_API_KEY is not set.")
        
    genai.configure(api_key=api_key)
    model = genai.GenerativeModel('gemini-2.0-flash')
    
    system_prompt = get_system_prompt(template_name=template)
    
    current_user_prompt = user_prompt
    code = ""
    
    for attempt in range(max_retries + 1):
        full_prompt = f"{system_prompt}\n\nUser request: {current_user_prompt}"
        
        response = model.generate_content(full_prompt)
        text = response.text
        
        # Extract code assuming it might be wrapped in markdown block
        if "```python" in text:
            text = text.split("```python")[1].split("```")[0].strip()
        elif "```" in text:
            text = text.split("```")[1].split("```")[0].strip()
        
        code = text
        
        # Validate
        result = validate_manim_code(code)
        if result["valid"]:
            return result["fixed_code"]
        
        # Build correction prompt for next iteration
        error_list = "\n".join(f"- {e}" for e in result["errors"])
        current_user_prompt = f"""
The following Manim code has errors that will cause runtime failures:
```python
{code}
```

Errors found:
{error_list}

Fix ONLY the errors listed above. Keep everything else identical.
Return only the corrected Python code, nothing else.
"""
        # Apply auto-fixes (like missing imports) to the code we send back or the final result
        code = result["fixed_code"]

    return code
