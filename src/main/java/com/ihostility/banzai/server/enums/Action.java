package com.ihostility.banzai.server.enums;

public enum Action {
    ROCK ("Камень!"),
    PAPER ("Бумага!"),
    SCISSORS ("Ножницы!");

    private String title;

    Action (String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public int compareAction(Action otherAction) {
        if (this == otherAction)
            return 0;

        switch (this) {
            case ROCK:
                return (otherAction == SCISSORS ? 1 : -1);
            case PAPER:
                return (otherAction == ROCK ? 1 : -1);
            case SCISSORS:
                return (otherAction == PAPER ? 1 : -1);
        }
        return 0;
    }
}
