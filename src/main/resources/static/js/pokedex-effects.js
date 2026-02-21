function updateBg() {
    const select = document.getElementById('region-select');
    const container = document.getElementById('region-bg');
    if (select && container) {
        container.className = 'filter-section bg-' + select.value;
    }
}

function shutDownTV() {
    const wrapper = document.getElementById('pokedexWrapper');
    const form = document.getElementById('logoutForm');

    document.body.style.backgroundColor = "#000";
    document.body.style.backgroundImage = "none";

    if (wrapper) {
        wrapper.classList.add('tv-off-animation');
    }

    setTimeout(() => {
        if (form) {
            form.submit();
        }
    }, 600);
}

function captureAnimation(event, element) {
    event.preventDefault();
    const url = element.getAttribute('href');
    const card = element.closest('.pokemon-card');
    const img = card.querySelector('.pokemon-img');

    document.body.style.backgroundColor = "#fff";

    img.style.transition = "all 0.5s ease-in";
    img.style.transform = "scale(0.1) rotate(360deg)";
    img.style.filter = "brightness(5) sepia(1) hue-rotate(100deg)";
    img.style.opacity = "0";

    setTimeout(() => {
        window.location.href = url;
    }, 500);
}