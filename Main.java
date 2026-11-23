import java.util.*;

public class Main {
    public static void main(String[] args) {

        Customer c = new Customer("Akhmad", 100000);
        Product p = new Product("Nasi Goreng", 18000);
        Product q = new Product("Es Teh", 5000);

        Cart cart = new Cart();
        cart.add(p, 2);
        cart.add(q, 1);

        Order o = new Order("ORD01", c, cart);

        o.addObserver(new Notif("Pelanggan"));
        o.addObserver(new Notif("Restoran"));
        o.addObserver(new Notif("Kurir"));

        int sub = o.subtotal();
        int ongkir = new Ongkir().hitung(0);
        int diskon = new Diskon().hitung(sub);
        int total = sub + ongkir - diskon;

        System.out.println("Subtotal : " + sub);
        System.out.println("Ongkir   : " + ongkir);
        System.out.println("Diskon   : " + diskon);
        System.out.println("Total    : " + total);

        Validasi v = new Menu();
        v.setNext(new Restoran())
         .setNext(new Saldo())
         .setNext(new Promo());

        v.proses(o, total);

        o.setState(new Valid());
        o.setState(new Proses());
        o.setState(new Siap());
        o.setState(new Antar());
        o.setState(new Selesai());

        System.out.println("Status akhir: " + o.status());

        Command cmd = new Batal(o);
        Order o2 = new Order("ORD02", c, cart);

        cmd = new Batal(o2);
        cmd.execute();
        System.out.println("Batal: " + o2.status());

        cmd.undo();
        System.out.println("Undo: " + o2.status());

        Order ulang = new Ulang(o).execute();
        System.out.println("Pesanan ulang: " + ulang.id);
    }
}

class Customer {
    String nama;
    int saldo;

    Customer(String n, int s) {
        nama = n;
        saldo = s;
    }
}

class Product {
    String nama;
    int harga;

    Product(String n, int h) {
        nama = n;
        harga = h;
    }
}

class Cart {
    List<Product> menu = new ArrayList<>();
    List<Integer> jumlah = new ArrayList<>();

    void add(Product p, int j) {
        menu.add(p);
        jumlah.add(j);
    }

    int total() {
        int t = 0;
        for (int i = 0; i < menu.size(); i++)
            t += menu.get(i).harga * jumlah.get(i);
        return t;
    }
}

class Order {
    String id;
    Customer customer;
    Cart cart;
    State state = new Baru();
    List<Observer> obs = new ArrayList<>();

    Order(String id, Customer c, Cart cart) {
        this.id = id;
        customer = c;
        this.cart = cart;
    }

    int subtotal() {
        return cart.total();
    }

    void addObserver(Observer o) {
        obs.add(o);
    }

    void setState(State s) {
        state = s;
        for (Observer o : obs)
            o.update(id, status());
    }

    String status() {
        return state.status();
    }
}

interface Observer {
    void update(String id, String status);
}

class Notif implements Observer {
    String tujuan;

    Notif(String t) {
        tujuan = t;
    }

    public void update(String id, String status) {
        System.out.println(tujuan + ": " + id + " - " + status);
    }
}

interface State {
    String status();
}

class Baru implements State {
    public String status() { return "Baru"; }
}

class Valid implements State {
    public String status() { return "Tervalidasi"; }
}

class Proses implements State {
    public String status() { return "Diproses"; }
}

class Siap implements State {
    public String status() { return "Siap"; }
}

class Antar implements State {
    public String status() { return "Diantar"; }
}

class Selesai implements State {
    public String status() { return "Selesai"; }
}

class BatalState implements State {
    public String status() { return "Dibatalkan"; }
}

interface Strategy {
    int hitung(int nilai);
}

class Ongkir implements Strategy {
    public int hitung(int nilai) {
        return 8000;
    }
}

class Diskon implements Strategy {
    public int hitung(int nilai) {
        return nilai * 10 / 100;
    }
}

abstract class Validasi {
    Validasi next;

    Validasi setNext(Validasi v) {
        next = v;
        return v;
    }

    void proses(Order o, int total) {
        cek(o, total);
        if (next != null)
            next.proses(o, total);
    }

    abstract void cek(Order o, int total);
}

class Menu extends Validasi {
    void cek(Order o, int total) {
        if (o.cart.menu.isEmpty())
            throw new RuntimeException("Menu kosong");
        System.out.println("Menu valid");
    }
}

class Restoran extends Validasi {
    void cek(Order o, int total) {
        System.out.println("Restoran valid");
    }
}

class Saldo extends Validasi {
    void cek(Order o, int total) {
        if (o.customer.saldo < total)
            throw new RuntimeException("Saldo kurang");
        System.out.println("Saldo valid");
    }
}

class Promo extends Validasi {
    void cek(Order o, int total) {
        System.out.println("Promo valid");
    }
}

interface Command {
    void execute();
    void undo();
}

class Batal implements Command {
    Order order;
    State sebelum;

    Batal(Order o) {
        order = o;
    }

    public void execute() {
        sebelum = order.state;
        order.setState(new BatalState());
    }

    public void undo() {
        order.setState(sebelum);
    }
}

class Ulang {
    Order order;

    Ulang(Order o) {
        order = o;
    }

    Order execute() {
        return new Order("ORD03", order.customer, order.cart);
    }
}
