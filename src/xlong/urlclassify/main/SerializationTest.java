package xlong.urlclassify.main;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import xlong.wm.sample.Label;
import xlong.wm.sample.Labels;
import xlong.wm.sample.Sample;
import xlong.wm.sample.SparseVector;
import xlong.wm.sample.Text;

public class SerializationTest {

	public static void main(String[] args) throws Exception {
		//save();
		load();
	}
	
	
	public static void save() throws Exception {
		// Label
		Label l1 = Labels.getLabel("testlabel");
		Label l2 = Labels.getLabel("testlabel2");
		Label l3 = Labels.getLabel("testlabel");	
		
		// Property
		Text t1 = new Text("text1");
		SparseVector s1 = new SparseVector("2 1 0.1 0 0.4");
		
		// Sample
		Text t2 = new Text("text2");
		SparseVector s2 = new SparseVector("2 1 0.5 0 0.1");
		Sample sample1 = new Sample(t2.getText(), s2);
		
		
		String fileName = "result/serialization.test";
		FileOutputStream fos = new FileOutputStream(fileName);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        
        // Label
        oos.writeObject(l1);
        oos.writeObject(l2);
        oos.writeObject(l3);
        oos.writeObject(t1);
        oos.writeObject(s1);
        oos.writeObject(sample1);
        
        oos.close();
	}
	
	public static void load() throws Exception {
		
		String fileName = "result/serialization.test";
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		
		// Label
		Label l1 = (Label) ois.readObject();
		Label l2 = (Label) ois.readObject();
		Label l3 = (Label) ois.readObject();
		System.out.println(l1.toString());
		System.out.println(l2.toString());
		System.out.println(l3.toString());
		System.out.println(l1 == l2);
		System.out.println(l1 == l3);
		
		// Property
		Text t1 = (Text) ois.readObject();
		SparseVector s1 = (SparseVector)  ois.readObject();
		System.out.println(t1.getOneLineString());
		System.out.println(s1.getOneLineString());
		
		// Sample
		Sample sample1 = (Sample) ois.readObject();
		System.out.println(sample1.toString());
		
		ois.close();
	}

}
