document.addEventListener("DOMContentLoaded", function () {
    const questions = document.querySelectorAll(".question-page");
    if (questions.length === 0) return;

    const nextBtn = document.getElementById("nextBtn");
    const prevBtn = document.getElementById("prevBtn");
    const submitBtn = document.getElementById("submitBtn");
    const pageNumbersContainer = document.getElementById("pageNumbers");

    let current = 0;

    function generatePageNumbers() {
        if (!pageNumbersContainer) return;

        pageNumbersContainer.innerHTML = "";

        questions.forEach((_, i) => {
            const span = document.createElement("span");
            span.textContent = i + 1;

            if (i === current) span.classList.add("active");

            span.addEventListener("click", () => {
                current = i;
                showQuestion(current);
            });

            pageNumbersContainer.appendChild(span);
        });
    }

    function showQuestion(index) {
        questions.forEach((q, i) => {
            q.classList.toggle("active", i === index);
        });

        if (prevBtn) prevBtn.style.display = index === 0 ? "none" : "inline-block";
        if (nextBtn) nextBtn.style.display = index === questions.length - 1 ? "none" : "inline-block";

        if (submitBtn) {
            submitBtn.style.display = index === questions.length - 1 ? "inline-block" : "none";
        }

        generatePageNumbers();
    }

    if (nextBtn) {
        nextBtn.addEventListener("click", () => {
            if (current < questions.length - 1) current++;
            showQuestion(current);
        });
    }

    if (prevBtn) {
        prevBtn.addEventListener("click", () => {
            if (current > 0) current--;
            showQuestion(current);
        });
    }

    showQuestion(current);
});