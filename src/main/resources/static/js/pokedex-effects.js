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