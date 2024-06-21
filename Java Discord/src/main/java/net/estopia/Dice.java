package net.estopia;
public class Dice {

    public Dice() {
        roll();
    }

    public int roll() {
        return (int) (Math.random() * 6) + 1;
    }
}
