document.addEventListener('play', (e) => {
    if (e.target.tagName.toLowerCase() === 'audio') {
        const players = document.querySelectorAll('audio');
        players.forEach(player => {
            if (player !== e.target) {
                player.pause();
            }
        });
    }
}, true);
