package jerry.test.JDKSerialization;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/*测试jdk自带的序列化对象的方式。 此方式必须实现接口Serializable
 * 
 * */

class Person implements Serializable {
	private String name;
	private int age;

	public Person() {
	}

	public Person(String str, int n) {
		System.out.println("Inside Person's Constructor");
		name = str;
		age = n;
	}

	String getName() {
		return name;
	}

	int getAge() {
		return age;
	}
}

public class SerializationTest {
	public static void main(String[] args) {
		SerializationTest ser = new SerializationTest();
		ser.savePerson();
		ser.restorePerson();
	}

	public void savePerson() {
		Person myPerson = new Person("Jay", 24);
		try {
			FileOutputStream fos = new FileOutputStream("myPerson.txt");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			System.out.println("Person--Jay,24---Written");
			System.out.println("Name is: " + myPerson.getName());
			System.out.println("Age is: " + myPerson.getAge());

			oos.writeObject(myPerson);
			oos.flush();
			oos.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void restorePerson() {
		try {
			FileInputStream fis = new FileInputStream("myPerson.txt");
			ObjectInputStream ois = new ObjectInputStream(fis);

			Person myPerson = (Person) ois.readObject();
			System.out.println("\n--------------------\n");
			System.out.println("Person--Jay,24---Restored");
			System.out.println("Name is: " + myPerson.getName());
			System.out.println("Age is: " + myPerson.getAge());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
