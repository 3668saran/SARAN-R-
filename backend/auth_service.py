from functools import wraps
from flask import request, jsonify
from backend.config import Config
import logging

logger = logging.getLogger(__name__)

# Sample in-memory user registry for authorized development & RBAC
DEV_USERS = {
    "dev-token-student": {
        "uid": "user-std-101",
        "name": "Alex Morgan",
        "email": "alex.morgan@university.edu",
        "role": "student",
        "collegeRegNumber": "REG-2024-CS-8921",
        "verified": True
    },
    "dev-token-staff": {
        "uid": "user-staff-301",
        "name": "Dr. Evelyn Vance",
        "email": "dr.vance@university.edu",
        "role": "staff",
        "verified": True
    },
    "dev-token-admin": {
        "uid": "user-admin-501",
        "name": "Dean Robert Hayes",
        "email": "dean.hayes@university.edu",
        "role": "admin",
        "verified": True
    }
}

def verify_token(token_str):
    if not token_str:
        return None
        
    # Check development test tokens
    if token_str in DEV_USERS:
        return DEV_USERS[token_str]
        
    # Attempt verification via Firebase Admin SDK
    try:
        from firebase_admin import auth
        decoded_token = auth.verify_id_token(token_str)
        return {
            "uid": decoded_token.get("uid"),
            "name": decoded_token.get("name", "Student"),
            "email": decoded_token.get("email", ""),
            "role": decoded_token.get("role", "student"),
            "verified": decoded_token.get("verified", False)
        }
    except Exception as e:
        logger.warning(f"Token verification error: {e}")
        return None

def get_current_user():
    auth_header = request.headers.get("Authorization", "")
    if auth_header.startswith("Bearer "):
        token = auth_header.split("Bearer ")[1].strip()
        return verify_token(token)
    return None

def require_auth(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        user = get_current_user()
        if not user:
            return jsonify({
                "success": False,
                "error": "Unauthorized. Please provide a valid Firebase authentication token."
            }), 401
        request.user = user
        return f(*args, **kwargs)
    return decorated_function

def require_role(allowed_roles):
    if isinstance(allowed_roles, str):
        allowed_roles = [allowed_roles]
        
    def decorator(f):
        @wraps(f)
        def decorated_function(*args, **kwargs):
            user = get_current_user()
            if not user:
                return jsonify({"success": False, "error": "Authentication required"}), 401
            
            user_role = user.get("role", "student")
            if user_role not in allowed_roles:
                return jsonify({
                    "success": False,
                    "error": f"Forbidden. Role '{user_role}' is not authorized to access this resource."
                }), 403
                
            request.user = user
            return f(*args, **kwargs)
        return decorated_function
    return decorator
