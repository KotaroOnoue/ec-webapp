document.addEventListener("DOMContentLoaded", function () {
    const errorMessage = document.getElementById("error-message");
    if (errorMessage) {
        errorMessage.focus();
        errorMessage.scrollIntoView({ behavior: "smooth", block: "center" });
    }

    document.querySelectorAll(".cart-view-trigger").forEach(function (trigger) {
        trigger.addEventListener("click", function (event) {
            const targetId = trigger.getAttribute("href");
            if (!targetId || !targetId.startsWith("#")) {
                return;
            }

            const target = document.querySelector(targetId);
            if (!target) {
                return;
            }

            event.preventDefault();
            target.scrollIntoView({ behavior: "smooth", block: "start" });
        });
    });
});