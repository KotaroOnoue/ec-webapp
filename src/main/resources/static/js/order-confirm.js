document.addEventListener("DOMContentLoaded", function () {
    const errorMessage = document.getElementById("error-message");
    if (errorMessage) {
        errorMessage.focus();
        errorMessage.scrollIntoView({ behavior: "smooth", block: "center" });
    }
});