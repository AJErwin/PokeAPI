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

function capturarEnBatalla(btn) {
    const ballContainer = document.getElementById('pokeball-container');
    const ball = document.getElementById('animating-ball');
    const pokemonSpriteBatalla = document.querySelector('.pokemon-sprite-battle');
    const dialogText = document.getElementById('dialogText');
    
    const id = btn.getAttribute('data-id');
    const nombre = btn.getAttribute('data-nombre');
    const urlImg = btn.getAttribute('data-img');

    if (ballContainer && ball) {
        ballContainer.style.display = 'block';
        ball.classList.add('throw-animation');
    }

    const formData = new FormData();
    formData.append('id', id);
    formData.append('nombre', nombre);
    formData.append('urlImagen', urlImg);

    fetch('/pokedex/guardar', { method: 'POST', body: formData })
    .then(response => response.text())
    .then(data => {
        if (data === "OK") {
            invocarEnSafari(id, nombre);

            setTimeout(() => {
                if (pokemonSpriteBatalla) {
                    pokemonSpriteBatalla.style.opacity = '0';
                    pokemonSpriteBatalla.style.transition = '0.5s';
                }
                
                if (ball) {
                    ball.classList.remove('throw-animation');
                    ball.classList.add('capture-shake');
                }

                setTimeout(() => {
                    if (dialogText) {
                        dialogText.innerText = "¡Gotcha! " + nombre.toUpperCase() + " capturado.";
                    }
                    btn.innerText = "ATRAPADO";
                    btn.disabled = true;
                    btn.style.backgroundColor = "#585858";
                }, 1200);
            }, 800);
        } else {
            alert("Error: " + data);
            if (ballContainer) ballContainer.style.display = 'none';
        }
    })
    .catch(err => console.error(err));
}

function eliminarDeFavoritos(btn) {
    const idPokemon = btn.getAttribute('data-id');
    const card = btn.closest('.col'); 

    const formData = new FormData();
    formData.append('idPokemon', idPokemon);

    fetch('/pokedex/eliminar', {
        method: 'POST',
        body: formData
    })
    .then(response => response.text())
    .then(data => {
        if (data === "OK") {
            card.style.transition = "0.5s";
            card.style.opacity = "0";
            card.style.transform = "scale(0.8)";
            setTimeout(() => card.remove(), 500); 
        } else {
            alert("Error al liberar: " + data);
        }
    })
    .catch(error => console.error('Error:', error));
}
