package jerry.test.JDKSerialization;

import java.io.Externalizable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

/*测试jdk自带的序列化对象的方式。 此方式必须实现接口Serializable
 * 
 * */

class UserInfo implements Externalizable {
	public String userName;
	public String userPass;
	public int userAge;

	public UserInfo() {
	}

	public UserInfo(String username, String userpass, int userage) {
		this.userName = username;
		this.userPass = userpass;
		this.userAge = userage;
	}

	// 当序列化对象时,该方法自动调用
	public void writeExternal(ObjectOutput out) throws IOException {
		System.out.println("现在执行序列化方法");
		// 可以在序列化时写非自身的变量
		Date d = new Date();
		out.writeObject(d);
		// 只序列化userName,userPass变量
		out.writeObject(userName);
		out.writeObject(userPass);
	}

	// 当反序列化对象时,该方法自动调用
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		System.out.println("现在执行反序列化方法");
		Date d = (Date) in.readObject();
		System.out.println(d);
		this.userName = (String) in.readObject();
		this.userPass = (String) in.readObject();
	}

	public String toString() {
		return "用户名: " + this.userName + ";密码：" + this.userPass + ";年龄：" + this.userAge;
	}
}

public class SerializationTest2 {
	public static void serialize(String fileName) {
		try {
			// 创建一个对象输出流，讲对象输出到文件
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));

			UserInfo user = new UserInfo("renyanwei", "888888", 20);
			out.writeObject(user); // 序列化一个会员对象

			out.close();
		} catch (Exception x) {
			System.out.println(x.toString());
		}

	}

	// 从文件反序列化到对象
	public static void deserialize(String fileName) {
		try {
			// 创建一个对象输入流，从文件读取对象
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));

			// 读取UserInfo对象并调用它的toString()方法
			UserInfo user = (UserInfo) (in.readObject());
			System.out.println(user.toString());

			in.close();
		} catch (Exception x) {
			System.out.println(x.toString());
		}

	}

	public static void main(String[] args) {

		serialize("test.txt");
		System.out.println("序列化完毕");

		deserialize("test.txt");
		System.out.println("反序列化完毕");
	}
}
