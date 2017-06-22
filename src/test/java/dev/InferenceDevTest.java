package dev;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.shared.WrappedIOException;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.XSD;

public class InferenceDevTest {

	private String NS = "http://localhost/demo.owl#";
	private String path_owl = "/home/darren/workspace/OntologyEditor/files/protege.owl";
	private String path_rule = "/home/darren/workspace/OntologyEditor/files/demo.txt";
	
	public static void main(String[] args) {
		try
		{
			
			InferenceDev mycode = new InferenceDev();
			//mycode.init();
			//if(mycode.verify())
			//{
				mycode.reason();
			//}
			
		}
		catch(WrappedIOException e)
		{
			System.out.println( e.getMessage() );
		}
		catch(Exception e)
		{
			System.out.println( e.getMessage() );
		}
		
	}
	
	protected void init()
	{
		try
		{
			//新增本體論 model
			OntModel model = ModelFactory.createOntologyModel();
			
			//新增類別 Animal
			OntClass Animal = model.createClass(this.NS + "Animal");
	
			//新增 Animal 子類別 Carnivore
			OntClass Carnivore = model.createClass(this.NS + "Carnivore");
			Animal.addSubClass(Carnivore);
			
			//新增 Animal 子類別 Herbivore
			OntClass Herbivore = model.createClass(this.NS + "Herbivore");
			Animal.addSubClass(Herbivore);
			
			//替 Animal 增加 name 屬性
			DatatypeProperty name = model.createDatatypeProperty(this.NS + "name");
			name.addDomain(Animal);
			name.addRange(XSD.xstring);
			
			//新增 Herbivore 的實體 Cow
			Individual Cow = Herbivore.createIndividual(this.NS + "Cow");
			
			//為 Cow 的屬性 name 新增為 Bob
			Cow.addProperty(name, this.NS + "Bob");
			
			//新增 Carnivore 的實體 Tiger
			Individual Tiger = Carnivore.createIndividual(this.NS + "Tiger");
			
			
			
			//新增類別 Plant
			OntClass Plant = model.createClass(this.NS + "Plant");
			
			//新增 Grass 實體
			Individual Grass = Plant.createIndividual(this.NS + "Grass");
	
			//新增 eat 屬性
			ObjectProperty eat = model.createObjectProperty(this.NS + "eat");
			eat.addDomain(Animal); //誰 eat
			eat.addRange(Plant); //eat 誰
			
			//為 Cow 的屬性 eat 對象設定為 Grass
			Cow.addProperty(eat, Grass); 
			
			//將 mode 儲存為 RDF 格式的檔案
			this.saveFile(model);
		}
		catch(Exception e)
		{
			System.out.println( e.getMessage() );
		}
	}
	
	//儲存檔案
	private void saveFile(OntModel model)
	{
		//Reference: https://jena.apache.org/documentation/io/rdf-input.html
		//Reference: https://jena.apache.org/documentation/io/rdf-output.html
		FileOutputStream out = null;
		try 
		{
			out = new FileOutputStream(this.path_owl);
			//model.write(out, "RDF/XML-ABBREV"); //"RDF/XML-ABBREV"為儲存的格式
			model.write(out, "RDF/XML");
		} 
		catch (IOException ignore) 
		{
			ignore.printStackTrace();
		}
	}
	
	//驗證推論 Model 的有效性
	protected boolean verify()
	{
		boolean bool = false;
		try
		{
			Model m = FileManager.get().loadModel(this.path_owl);
			InfModel infmodel = ModelFactory.createRDFSModel(m);
			ValidityReport validity = infmodel.validate();
			if (validity.isValid()) 
			{
			    System.out.println("Validity is OK");
			    bool = true;
			} 
			else 
			{
			    System.out.println("Conflicts");
			    for (Iterator i = validity.getReports(); i.hasNext(); ) 
			    {
			        System.out.println(" - " + i.next());
			    }
			}
		}
		catch(WrappedIOException e)
		{
			System.out.println( e.getMessage() );
		}
		catch(Exception e)
		{
			System.out.println( e.getMessage() );
		}
		return bool;
	}
	
	//進行推論
	protected void reason()
	{
		try
		{
			Model model = ModelFactory.createDefaultModel();
			model.read( this.path_owl );
			
			Reasoner reasoner = new GenericRuleReasoner( Rule.rulesFromURL( this.path_rule ) );
			
			InfModel infModel = ModelFactory.createInfModel( reasoner, model );

			// print out the statements in the model
			StmtIterator it = infModel.listStatements();
			
			while ( it.hasNext() )
			{
				Statement stmt = it.nextStatement();
				
				Resource subject = stmt.getSubject();
				Property predicate = stmt.getPredicate();
				RDFNode object = stmt.getObject();
				
				Pattern pattern = Pattern.compile("http:\\/\\/localhost\\/");
				Matcher matcher = pattern.matcher(subject.toString());
				if(matcher.find())
				{
					System.out.println( "[" + subject.toString() + "] => [" + predicate.toString() + "] => [ " + object.toString() + "]" );
				}
				
				//System.out.println( subject.toString() + " " + predicate.toString() + " " + object.toString() );
			}
			
//			Resource config = ModelFactory
//					.createDefaultModel()
//					.createResource()
//					.addProperty(ReasonerVocabulary.PROPsetRDFSLevel, "simple");
//			Reasoner reasoner = RDFSRuleReasonerFactory.theInstance().create(config);
			//InfModel inf = ModelFactory.createInfModel(reasoner, m);
			//System.out.println(inf.toString());

			
//			String queryString = "PREFIX NS:  <http://www.example.com/your_ontology.owl#>\n"
//					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
//					+ "SELECT ?animal \n" 
//					+ "WHERE{ ?animal NS:hunt ?prey . ?prey rdf:type NS:herbivore   }";
//			Query query = QueryFactory.create(queryString);
//			QueryExecution qexec = QueryExecutionFactory.create(query, inf);
//			ResultSet results = qexec.execSelect();
//			System.out.println(results);
//			
//			ResultSetFormatter.outputAsJSON(System.out, results);
//			
//			FileOutputStream out = null;
//			try 
//			{
//				out = new FileOutputStream("D:\\result.owl");
//				inf.write(out, "RDF/XML-ABBREV"); //"RDF/XML-ABBREV"為儲存的格式
//			} 
//			catch (IOException ignore) 
//			{
//				ignore.printStackTrace();
//
//			}

		}
		catch(WrappedIOException e)
		{
			System.out.println( e.getMessage() );
		}
		catch(Exception e)
		{
			System.out.println( e.getMessage() );
		}
	}

}

