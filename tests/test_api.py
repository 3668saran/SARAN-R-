import pytest
from app import create_app

@pytest.fixture
def client():
    app = create_app()
    app.config["TESTING"] = True
    with app.test_client() as client:
        yield client

def test_health_endpoint(client):
    res = client.get("/health")
    assert res.status_code == 200
    data = res.get_json()
    assert data["status"] == "healthy"
    assert data["app"] == "StudyMate AI"

def test_unauthorized_access(client):
    # Attempting to post to community without token must yield 401
    res = client.post("/api/community/posts", json={"title": "Test", "content": "Sample content"})
    assert res.status_code == 401

def test_ai_status(client):
    res = client.get("/api/ai/status")
    assert res.status_code == 200
    data = res.get_json()
    assert data["status"] == "online"

def test_ai_chat_authorized(client):
    headers = {"Authorization": "Bearer dev-token-student"}
    res = client.post("/api/ai/chat", json={
        "prompt": "Explain Python list comprehensions",
        "subject": "Computer Science"
    }, headers=headers)
    assert res.status_code == 200
    data = res.get_json()
    assert data["success"] is True
    assert "response" in data

def test_community_workflow(client):
    headers = {"Authorization": "Bearer dev-token-student"}
    
    # Create post
    res = client.post("/api/community/posts", json={
        "title": "Mastering Concurrency in Python",
        "content": "A detailed discussion on threading vs multiprocessing in Python 3.12.",
        "subject": "Computer Science"
    }, headers=headers)
    assert res.status_code == 201
    post_id = res.get_json()["post"]["id"]

    # Upvote post
    upvote_res = client.post(f"/api/community/posts/{post_id}/upvote", headers=headers)
    assert upvote_res.status_code == 200
    assert upvote_res.get_json()["hasUpvoted"] is True

def test_quiz_scoring(client):
    headers = {"Authorization": "Bearer dev-token-student"}
    
    # Submit answers to quiz-py-1 (Q1: tuple is idx 2, Q2: hash is idx 0)
    res = client.post("/api/quizzes/quiz-py-1/submit", json={
        "answers": {"0": 2, "1": 0}
    }, headers=headers)
    assert res.status_code == 200
    data = res.get_json()
    assert data["score"] == 2
    assert data["percentage"] == 100

def test_trustcheck_workflow_and_rbac(client):
    student_headers = {"Authorization": "Bearer dev-token-student"}
    staff_headers = {"Authorization": "Bearer dev-token-staff"}

    # 1. Student submits document
    sub_res = client.post("/api/trustcheck/submit", json={
        "collegeRegNumber": "REG-2024-CS-9912",
        "documentType": "Semester Grade Marksheet",
        "documentName": "marksheet_scan.pdf"
    }, headers=student_headers)
    assert sub_res.status_code == 201
    sub_id = sub_res.get_json()["submission"]["id"]

    # 2. Student attempts to access staff review queue -> Must fail 403 Forbidden
    forbidden_res = client.get("/api/trustcheck/staff/queue", headers=student_headers)
    assert forbidden_res.status_code == 403

    # 3. Staff accesses queue -> Success 200
    queue_res = client.get("/api/trustcheck/staff/queue", headers=staff_headers)
    assert queue_res.status_code == 200

    # 4. Staff reviews and approves submission
    review_res = client.post("/api/trustcheck/staff/review", json={
        "submissionId": sub_id,
        "approved": True,
        "remarks": "Official registrar watermark verified."
    }, headers=staff_headers)
    assert review_res.status_code == 200
    assert review_res.get_json()["submission"]["status"] == "verified"

def test_profile_and_marks_workflow(client):
    headers = {"Authorization": "Bearer dev-token-student"}
    
    # PUT profile
    put_res = client.put("/api/profile", json={
        "name": "Alex Morgan",
        "college": "National Institute of Science",
        "course": "Computer Science"
    }, headers=headers)
    assert put_res.status_code == 200

    # GET profile
    get_res = client.get("/api/profile", headers=headers)
    assert get_res.status_code == 200
    assert get_res.get_json()["name"] == "Alex Morgan"

    # POST marks
    marks_res = client.post("/api/marks", json={
        "subject": "Distributed Systems",
        "marks": 95,
        "maximumMarks": 100
    }, headers=headers)
    assert marks_res.status_code == 201
    assert marks_res.get_json()["verificationStatus"] == "pending"

    # GET marks
    get_marks_res = client.get("/api/marks", headers=headers)
    assert get_marks_res.status_code == 200
    assert len(get_marks_res.get_json()) >= 1

    # POST /api/ai/ask
    ask_res = client.post("/api/ai/ask", json={
        "question": "Explain Dijkstra algorithm in short"
    }, headers=headers)
    assert ask_res.status_code == 200
    assert "answer" in ask_res.get_json()

