document.addEventListener("DOMContentLoaded", () => {
    const sections = document.querySelectorAll('.feature-section');
    const subscribeSection = document.querySelector('.subscribe-section');

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

        if (subscribeSection) {
            const sectionTop = subscribeSection.getBoundingClientRect().top;
            if (sectionTop < triggerBottom) {
                subscribeSection.classList.add("visible");
            } else {
                subscribeSection.classList.remove("visible");
            }
        }
    };

    window.addEventListener("scroll", revealOnScroll);
    revealOnScroll();
});