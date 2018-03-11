package wrappy;

public class SynchronousApi {

    public String getName() {
        return SynchronousApi.class.getSimpleName();
    }

    public int add(int a, int b) {
        return a + b;
    }

    public void printName(String name) {
        System.out.println("My name is " + name);
    }

}
