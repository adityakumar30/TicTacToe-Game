"");
        currentPlayer = 'X';
        statusLabel.setText("Player X's Turn");
        if (fullReset) {
            scoreX = 0;
            scoreO = 0;
            updateScore();
        }
    }

    private void updateScore() {
        scoreLabel.setText("Score - X: " + scoreX + " | O: " + scoreO);
    }