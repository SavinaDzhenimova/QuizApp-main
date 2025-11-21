document.addEventListener("DOMContentLoaded", () => {
    const sections = document.querySelectorAll('.feature-section');

    const revealOnScroll = () => {
        const triggerBottom = window.innerHeight * 0.85;

        sections.forEach(section => {
            const sectionTop = section.getBoundingClientRect().top;

            if (sectionTop < triggerBottom) {
                section.classList.add("visible");
            } else {
                section.classList.remove("visible");
            }
        });
    };

    window.addEventListener("scroll", revealOnScroll);
    revealOnScroll();
});