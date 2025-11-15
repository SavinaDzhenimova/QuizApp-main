document.addEventListener("DOMContentLoaded", () => {
    const modal = document.getElementById("editModal");
    const closeBtn = document.querySelector(".modal .close");
    const editForm = document.getElementById("editForm");

    const setFormValues = (id, name, description) => {
        document.getElementById("edit-id").value = id;
        document.getElementById("edit-name").value = name;
        document.getElementById("edit-description").value = description;

        editForm.action = `/categories/update/${id}`;
        editForm.method = "post";
    };

    document.querySelectorAll(".edit-btn").forEach(btn => {
        btn.addEventListener("click", () => {
            setFormValues(btn.dataset.id, btn.dataset.name, btn.dataset.description);
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
        const name = document.getElementById("edit-name").value;
        const description = document.getElementById("edit-description").value;

        setFormValues(id, name, description);
        modal.style.display = "flex";
    }
});