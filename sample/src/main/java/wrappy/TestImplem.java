package wrappy;

import me.dfournier.wrappy.annotations.CustomWrapper;

public class TestImplem {

    @Wrappy(processor = "Empty")
    public static SynchronousApiSimpleWrapper getSimpleWrapper(SynchronousApi api) {
        return new SynchronousApiSimpleWrapper(api);
    }

    @Wrappy(processor = "RxJava2")
    public static SynchronousApiRxWrapper getRxWrapper(SynchronousApi api) {
        return new SynchronousApiRxWrapper(api);
    }

    public static void main(String[] args) {
        getRxWrapper(new SynchronousApi())
                .printName("John").subscribe();

        int result = getSimpleWrapper(new SynchronousApi())
                .add(2, 6);
        System.out.println("The result is " + result);
    }

}