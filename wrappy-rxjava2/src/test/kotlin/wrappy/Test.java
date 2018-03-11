package wrappy;

import io.reactivex.Single;
import org.jetbrains.annotations.Contract;

public class Test {

    Single<Integer> t;
    Single<int[]> a;


    public Integer get() {
        return test();
    }

    private int test() {
        return 10;
    }

}
