// Add your JS scripts heredocument.addEventListener("DOMContentLoaded", function() {
    console.log("Scripts loaded successfully.");

    // Example: add event listener for all links in the navigation
    const navLinks = document.querySelectorAll("nav a");
    navLinks.forEach(link => {
        link.addEventListener("click", function(e) {
            // Prevent default behavior for demonstration or debugging
            e.preventDefault();
            const url = this.getAttribute("href");
            console.log("Navigating to: " + url);
            // Redirect after short delay
            setTimeout(() => {
                window.location.href = url;
            }, 100);
        });
    });
