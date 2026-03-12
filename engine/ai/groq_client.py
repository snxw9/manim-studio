import asyncio
import os
from groq import AsyncGroq

client = AsyncGroq(api_key=os.environ.get("GROQ_API_KEY", ""))

async def generate(prompt: str, model: str = "llama-3.3-70b-versatile") -> str:
    last_error = None
    for attempt in range(3):
        try:
            response = await client.chat.completions.create(
                model=model,
                messages=[
                    {
                        "role": "system",
                        "content": "You are an expert Manim animator. Output only valid Python code. No explanation, no markdown fences."
                    },
                    {"role": "user", "content": prompt}
                ],
                temperature=0.2,
                max_tokens=4096,
            )
            text = response.choices[0].message.content.strip()
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
                    wait = (attempt + 1) * 5
                    print(f"[groq] Rate limited, waiting {wait}s before retry {attempt+1}/2...")
                    await asyncio.sleep(wait)
                    continue
            raise
    
    raise last_error
