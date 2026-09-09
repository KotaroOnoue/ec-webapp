document.addEventListener("DOMContentLoaded", function () {
    const errorMessage = document.getElementById("error-message");
    if (errorMessage) {
        errorMessage.focus();
        errorMessage.scrollIntoView({ behavior: "smooth", block: "center" });
    }

    document.querySelectorAll(".detail-button").forEach(function (button) {
        button.addEventListener("click", function () {
            const productName = button.dataset.productName || "商品";
            window.alert(productName + " の詳細画面は未作成です。");
        });
    });

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