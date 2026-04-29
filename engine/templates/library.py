TEMPLATES: dict[str, dict] = {}

def get_template(template_id: str) -> dict | None:
    return TEMPLATES.get(template_id)

def list_templates() -> list[dict]:
    return []
