import re
import html

def sanitize_string(text, max_len=1000):
    if not isinstance(text, str):
        return ""
    # Strip HTML tags or escape them to prevent stored XSS
    escaped = html.escape(text.strip())
    return escaped[:max_len]

def validate_reg_number(reg_no):
    if not reg_no or not isinstance(reg_no, str):
        return False, "Registration number is required"
    reg_no = reg_no.strip()
    if len(reg_no) < 4 or len(reg_no) > 30:
        return False, "Registration number must be between 4 and 30 characters"
    if not re.match(r'^[A-Za-z0-9\-_/]+$', reg_no):
        return False, "Registration number contains invalid characters"
    return True, reg_no

def validate_post_data(data):
    if not isinstance(data, dict):
        return False, "Invalid JSON payload"
    title = data.get("title", "").strip()
    content = data.get("content", "").strip()
    subject = data.get("subject", "General").strip()
    
    if len(title) < 5 or len(title) > 200:
        return False, "Title must be between 5 and 200 characters"
    if len(content) < 10 or len(content) > 5000:
        return False, "Content must be between 10 and 5,000 characters"
        
    return True, {
        "title": sanitize_string(title, 200),
        "content": sanitize_string(content, 5000),
        "subject": sanitize_string(subject, 50)
    }

def validate_allowed_file(filename, allowed_extensions=None):
    if allowed_extensions is None:
        allowed_extensions = {'pdf', 'png', 'jpg', 'jpeg'}
    if '.' not in filename:
        return False
    ext = filename.rsplit('.', 1)[1].lower()
    return ext in allowed_extensions
