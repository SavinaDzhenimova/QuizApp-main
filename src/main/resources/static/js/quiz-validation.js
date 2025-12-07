document.addEventListener("DOMContentLoaded", function () {
    const submitBtn = document.getElementById("submitBtn");
    const questions = document.querySelectorAll(".question-page");

    if (!submitBtn || questions.length === 0) return;

    submitBtn.disabled = true;

    function checkAllAnswered() {
        let allAnswered = true;

        questions.forEach(question => {
            const firstRadio = question.querySelector('input[type="radio"]');
            if (!firstRadio) return;

            const name = firstRadio.name;
            const isAnswered = question.querySelector(`input[name="${name}"]:checked`);

            if (!isAnswered) {
                allAnswered = false;
            }
        });

        submitBtn.disabled = !allAnswered;
    }

    questions.forEach(question => {
        const radios = question.querySelectorAll('input[type="radio"]');
        radios.forEach(radio => {
            radio.addEventListener("change", checkAllAnswered);
        });
    });

    checkAllAnswered();
});