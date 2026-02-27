let idPokemonActual = 0;
let myChart = null;

document.addEventListener('DOMContentLoaded', () => {
    const savedTheme = localStorage.getItem('pokedexTheme');
    if (savedTheme) setSceneTheme(savedTheme);

    const mainPkmn = document.getElementById('mainPkmn');
    if (mainPkmn && window.location.pathname.includes('/detalle/')) {
        const urlParts = window.location.pathname.split('/');
        const id = urlParts[urlParts.length - 1];
        playPokemonCry(id);
    }
});

function playPokemonCry(id) {
    const cry = new Audio(`https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/cries/${id}.ogg`);
    cry.volume = 0.4;
    cry.play().catch(() => console.log("Audio no disponible"));
}

function setSceneTheme(theme) {
    document.body.classList.remove('theme-night', 'theme-gameboy');
    if (theme !== 'default') document.body.classList.add('theme-' + theme);
    localStorage.setItem('pokedexTheme', theme);
}

function setSpriteStyle(style) {
    const pkmn = document.getElementById('mainPkmn');
    if (!pkmn) return;
    pkmn.classList.remove('sprite-retro', 'sprite-shadow');
    if (style === 'retro') pkmn.classList.add('sprite-retro');
    if (style === 'shadow') pkmn.classList.add('sprite-shadow');
}

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
    if (wrapper) wrapper.classList.add('tv-off-animation');
    setTimeout(() => {
        if (form) form.submit();
        else window.location.href = "/logout";
    }, 600);
}

function captureAnimation(event, element) {
    event.preventDefault();
    const url = element.getAttribute('href');
    const parts = url.split('/');
    const id = parts[parts.length - 1];
    const card = element.closest('.pokemon-card');
    const img = card ? card.querySelector('.pokemon-img') : null;

    playPokemonCry(id);

    document.body.style.backgroundColor = "#fff";
    if (img) {
        img.style.transition = "all 0.5s ease-in";
        img.style.transform = "scale(0.1) rotate(360deg)";
        img.style.filter = "brightness(5) sepia(1) hue-rotate(100deg)";
        img.style.opacity = "0";
    }
    setTimeout(() => {
        window.location.href = url;
    }, 500);
}

function capturarEnBatalla(btn) {
    const ballContainer = document.getElementById('pokeball-container');
    const ball = document.getElementById('animating-ball');
    const pokemonSprite = document.getElementById('mainPkmn') || document.querySelector('.pokemon-sprite-battle') || document.querySelector('.pokemon-img');
    const dialogText = document.getElementById('dialogText');
    const infoPanel = document.getElementById('infoPanel');
    const id = btn.getAttribute('data-id');
    const nombre = btn.getAttribute('data-nombre');
    const urlImg = btn.getAttribute('data-img');

    if (infoPanel && infoPanel.classList.contains('expanded')) toggleDetails();
    if (ballContainer && ball) {
        ballContainer.style.display = 'block';
        ball.classList.add('throw-animation');
    }

    const formData = new FormData();
    formData.append('id', id);
    formData.append('nombre', nombre);
    formData.append('urlImagen', urlImg);

    fetch('/pokedex/guardar', {method: 'POST', body: formData})
        .then(response => response.text())
        .then(data => {
            if (data === "OK") {
                setTimeout(() => {
                    if (pokemonSprite) {
                        pokemonSprite.style.transition = '0.5s';
                        pokemonSprite.style.opacity = '0';
                        pokemonSprite.style.transform = 'scale(0.1) rotate(360deg)';
                    }
                    if (ball) {
                        ball.classList.remove('throw-animation');
                        ball.classList.add('capture-shake');
                    }
                    setTimeout(() => {
                        if (dialogText) dialogText.innerText = "¡Gotcha! " + nombre.toUpperCase() + " capturado.";
                        btn.innerText = "ATRAPADO";
                        btn.disabled = true;
                        btn.style.backgroundColor = "#585858";
                        setTimeout(() => {
                            if (ballContainer) ballContainer.style.opacity = '0';
                        }, 1000);
                    }, 1200);
                }, 800);
            } else {
                alert("Error: " + data);
                if (ballContainer) ballContainer.style.display = 'none';
            }
        });
}

function eliminarDeFavoritos(btn) {
    const idPokemon = btn.getAttribute('data-id');
    const card = btn.closest('.col') || btn.closest('.pokemon-card');
    const formData = new FormData();
    formData.append('idPokemon', idPokemon);
    fetch('/pokedex/eliminar', {method: 'POST', body: formData})
        .then(response => response.text())
        .then(data => {
            if (data === "OK") {
                if (card) {
                    card.style.transition = "0.5s";
                    card.style.opacity = "0";
                    card.style.transform = "scale(0.8)";
                    setTimeout(() => card.remove(), 500);
                }
            } else {
                alert("Error al liberar: " + data);
            }
        });
}

function toggleDetails() {
    const info = document.getElementById('infoPanel');
    const extra = document.getElementById('extraData');
    if (!info || !extra) return;
    info.classList.toggle('expanded');
    const isExpanded = info.classList.contains('expanded');
    extra.style.display = isExpanded ? 'block' : 'none';
    if (isExpanded) setTimeout(initChart, 50);
}

function switchTab(tabId) {
    document.querySelectorAll('.mini-content').forEach(c => c.classList.remove('active-content'));
    document.querySelectorAll('.btn-tab-mini').forEach(b => b.classList.remove('active'));
    const targetTab = document.getElementById(tabId);
    if (targetTab) {
        targetTab.classList.add('active-content');
        if (event && event.target) event.target.classList.add('active');
        if (tabId === 'miniStats') setTimeout(initChart, 10);
    }
}

function changeMainModel(url, isShiny) {
    const mainPkmn = document.getElementById('mainPkmn');
    const nameLabel = document.getElementById('displayName');
    if (mainPkmn) {
        mainPkmn.style.opacity = '0';
        setTimeout(() => {
            mainPkmn.src = url;
            mainPkmn.style.opacity = '1';
            if (nameLabel) nameLabel.style.color = isShiny ? '#b08800' : '#333';
        }, 200);
    }
}

function initChart() {
    const ctx = document.getElementById('statsChart');
    if (!ctx || typeof statsData === 'undefined') return;
    if (myChart) myChart.destroy();
    myChart = new Chart(ctx.getContext('2d'), {
        type: 'radar',
        data: {
            labels: ['HP', 'ATK', 'DEF', 'SAT', 'SDF', 'SPD'],
            datasets: [{
                data: statsData,
                backgroundColor: 'rgba(56, 176, 168, 0.4)',
                borderColor: '#38b0a8',
                borderWidth: 2,
                pointRadius: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                r: {
                    angleLines: {color: '#585858'},
                    grid: {color: '#585858'},
                    pointLabels: {font: {family: 'Press Start 2P', size: 6}},
                    suggestedMin: 0,
                    suggestedMax: 150,
                    ticks: {display: false}
                }
            },
            plugins: {legend: {display: false}}
        }
    });
}

function cargarTrivia() {
    fetch('/pokedex/api/trivia-dia')
        .then(res => res.json())
        .then(pokemon => {
            if (pokemon && pokemon.urlImagen) {
                idPokemonActual = pokemon.id;
                const img = document.getElementById('triviaImg');
                const formDiv = document.getElementById('triviaForm');
                const resultDiv = document.getElementById('triviaResult');
                const input = document.getElementById('triviaInput');
                img.src = pokemon.urlImagen;
                img.style.filter = "brightness(0)";
                formDiv.style.display = 'block';
                resultDiv.style.display = 'none';
                input.value = "";
                document.getElementById('modalTrivia').style.display = 'flex';
            }
        });
}

function validarRespuestaTrivia() {
    const input = document.getElementById('triviaInput');
    const intento = input ? input.value : "";
    if (!intento.trim()) return;
    const formData = new FormData();
    formData.append('nombreIntento', intento);
    formData.append('idPokemon', idPokemonActual);
    fetch('/pokedex/api/trivia-validar', {method: 'POST', body: formData})
        .then(res => res.json())
        .then(data => {
            const img = document.getElementById('triviaImg');
            const msg = document.getElementById('triviaMsg');
            const resultDiv = document.getElementById('triviaResult');
            const formDiv = document.getElementById('triviaForm');
            if (img) img.style.filter = "brightness(1)";
            if (formDiv) formDiv.style.display = 'none';
            if (resultDiv) resultDiv.style.display = 'block';
            if (msg) {
                msg.innerText = data.success ? "¡CORRECTO! ES " + data.nombreReal : "¡OH NO! ERA " + data.nombreReal;
                msg.style.color = data.success ? "#38b0a8" : "#ff1100";
                if (data.success) playPokemonCry(idPokemonActual);
            }
        });
}

function cerrarTrivia() {
    const modal = document.getElementById('modalTrivia');
    if (modal) modal.style.display = 'none';
}

function startApp() {
    const container = document.getElementById('intro-container');
    const video = document.getElementById('intro-video');
    if (video) {
        video.muted = false;
        video.volume = 0.8;
    }
    if (container) container.classList.add('fade-out');
    sessionStorage.setItem('introPlayed', 'true');
    setTimeout(() => {
        if (container) container.style.display = 'none';
        if (video) video.pause();
    }, 1200);
}