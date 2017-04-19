package packagedependencycycle.packagec;

import packagedependencycycle.packagea.ClassA;
import packagedependencycycle.packageb.ClassB;

public class ClassC {

	public ClassA toClassA() {
		return new ClassA();
	}

	public ClassB toClassB() {
		return new ClassB();
	}
}
