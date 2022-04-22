package currencyConverter;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.*;
import org.junit.jupiter.api.Test;

public class MainWindowConvertTest {
	
	private String devise1;
	private String devise2;
	private ArrayList<Currency> devises;
	private Double montant;
	private MainWindow fenetrePrincipale;
	
	@Before
	public void setUp() {
		devise1 = "USD";
		devise2 = "CAD";
		devises = Currency.init();
		montant = 100.0;
		fenetrePrincipale = new MainWindow();
	}
	
	@Test
	public void testDevise1_bonContenu()
	{
		// Arrange.
		
	    
	    // Act.
		
		
		// Assert.	
	
		
	}
	
	@Test //On ne trouvera pas le bon contenu de la methode s'il y a plusieurs accolades sur la 1ere ligne
	public void testDevise1_mauvaisContenu()
	{
		// Arrange.
		
	    
	    // Act.
		final Double resultat = fenetrePrincipale.convert(devise1, devise2, devises, montant);
		
		// Assert.
	   assertNotEquals(resultat, montant);
		
	}
	
	@Test
	public void testTrouverSignature_bonneSignature() throws IOException
	{
		// Arrange.
		
		// Act.
					
		// Assert.
		
	}
	
	
	
}

