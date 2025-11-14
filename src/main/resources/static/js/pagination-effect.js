document.addEventListener("DOMContentLoaded", function () {
    const questions = document.querySelectorAll(".question-page");
    const nextBtn = document.getElementById("nextBtn");
    const prevBtn = document.getElementById("prevBtn");
    const submitBtn = document.getElementById("submitBtn");

    let current = 0;

    function showQuestion(index) {
        questions.forEach((q, i) => {
            q.classList.toggle("active", i === index);
        });

        prevBtn.style.display = index === 0 ? "none" : "inline-block";
        nextBtn.style.display = index === questions.length - 1 ? "none" : "inline-block";
        submitBtn.style.display = index === questions.length - 1 ? "inline-block" : "none";
    }

    nextBtn.addEventListener("click", () => {
        if (current < questions.length - 1) current++;
        showQuestion(current);
    });

    prevBtn.addEventListener("click", () => {
        if (current > 0) current--;
        showQuestion(current);
    });

    showQuestion(current);
});