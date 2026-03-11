import asyncio
import google.generativeai as genai
import os

genai.configure(api_key=os.environ.get("GEMINI_API_KEY", ""))

async def generate(prompt: str, model: str = "gemini-2.0-flash") -> str:
    client = genai.GenerativeModel(
        model,
        system_instruction=(
            "You are an expert Manim animator. Output only valid Python code. No explanation."
        )
    )
    
    last_error = None
    for attempt in range(3):
        try:
            response = await asyncio.to_thread(
                client.generate_content,
                prompt,
                generation_config=genai.types.GenerationConfig(
                    temperature=0.2,
                    max_output_tokens=4096,
                ),
            )
            text = response.text.strip()
            # Strip markdown code fences if present
            if text.startswith("```python"):
                text = text[9:]
            if text.startswith("```"):
                text = text[3:]
            if text.endswith("```"):
                text = text[:-3]
            return text.strip()

        except Exception as e:
            last_error = e
            err_str = str(e)
            if "429" in err_str or "quota" in err_str.lower():
                if attempt < 2:
                    wait = (attempt + 1) * 10  # 10s, 20s
                    print(f"[gemini] Rate limited, waiting {wait}s before retry {attempt+1}/2...")
                    await asyncio.sleep(wait)
                    continue
            raise  # Non-quota error — don't retry
    
    raise last_error
