import google.generativeai as genai
import os
import asyncio

async def generate_manim_code(prompt: str, model: str = "gemini-2.0-flash") -> str:
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        raise ValueError("GEMINI_API_KEY is not set.")
        
    genai.configure(api_key=api_key)
    client = genai.GenerativeModel(model)
    
    # Run in thread to not block event loop (client.generate_content is blocking)
    response = await asyncio.to_thread(
        client.generate_content,
        prompt,
        generation_config=genai.types.GenerationConfig(
            temperature=0.2,
            max_output_tokens=4096,
        )
    )
    
    text = response.text
    if "```python" in text:
        text = text.split("```python")[1].split("```")[0].strip()
    elif "```" in text:
        text = text.split("```")[1].split("```")[0].strip()
        
    return text.strip()
