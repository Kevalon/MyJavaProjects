package com.company;

public class Node {
    public Integer amount;
    public Character symbol;
    public Node left, right;

    public int getAmount() {
        return amount;
    }

    Node() {
        this.symbol = null;
    }

    Node(Node l, Node r) {
        left = l;
        right = r;
        amount = l.amount + r.amount;
        symbol = null;
    }
}
