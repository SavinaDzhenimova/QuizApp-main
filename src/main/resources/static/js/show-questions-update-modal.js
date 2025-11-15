document.addEventListener("DOMContentLoaded", () => {
    const modal = document.getElementById("editModal");
    const closeBtn = document.querySelector(".modal .close");
    const editForm = document.getElementById("editForm");

    const setFormValues = (id, questionText, categoryName, correctAnswer, options) => {
        document.getElementById("edit-id").value = id;
        document.getElementById("edit-question-text").value = questionText;
        document.getElementById("edit-category-name").value = categoryName;
        document.getElementById("edit-correct-answer").value = correctAnswer;
        document.getElementById("edit-options").value = options;

        editForm.action = `/questions/update/${id}`;
        editForm.method = "post";
    };

    document.querySelectorAll(".edit-btn").forEach(btn => {
        btn.addEventListener("click", () => {
            setFormValues(btn.dataset.id, btn.dataset.questionText, btn.dataset.categoryName,
                btn.dataset.correctAnswer, btn.dataset.options);
            modal.style.display = "flex";
        });
    });

    closeBtn.onclick = () => modal.style.display = "none";
    window.onclick = e => {
        if (e.target === modal) modal.style.display = "none";
    };

    const openModal = modal.dataset.openModal === 'true';
    if (openModal) {
        const id = document.getElementById("edit-id").value;
        const questionText = document.getElementById("edit-question-text").value;
        const categoryName = document.getElementById("edit-category-name").value;
        const correctAnswer = document.getElementById("edit-correct-answer").value;
        const options = document.getElementById("edit-options").value;

        setFormValues(id, questionText, categoryName, correctAnswer, options);
        modal.style.display = "flex";
    }
});