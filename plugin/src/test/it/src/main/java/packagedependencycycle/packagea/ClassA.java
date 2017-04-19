package packagedependencycycle.packagea;

import packagedependencycycle.packageb.ClassB;
import packagedependencycycle.packagec.ClassC;

public class ClassA {

	public ClassB toClassB() {
		return new ClassB();
	}
	
	public ClassC toClassC() {
		return new ClassC();
	}
}
