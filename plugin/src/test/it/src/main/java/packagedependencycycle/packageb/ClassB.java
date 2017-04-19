package packagedependencycycle.packageb;

import packagedependencycycle.packagea.ClassA;
import packagedependencycycle.packagec.ClassC;

public class ClassB {

	public ClassA toClassA() {
		return new ClassA();
	}

	public ClassC toClassC() {
		return new ClassC();
	}
}
