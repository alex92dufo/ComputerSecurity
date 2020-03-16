/* Alexandre Dufour, p1054564
 * Jean-Fran�ois Blanchette, p1099987 
 */

import java.io.IOException; 
import java.nio.charset.*; 
import java.nio.file.Files; 
import java.nio.file.Paths;
import java.util.Arrays;
import java.io.PrintWriter;

public class Decrypt{
	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	//� partir de la clef toruv�, on d�crypte le texte pour v�rifier si le r�sultat donne un texte lisible.
	public static String decrypt(String text, String key){
		String result = "";
		
		text = text.toLowerCase();
		
		int keyProgress = 0;
		
		for (int i = 0; i < text.length(); i++) {
			if(text.charAt(i) >= 'a' && text.charAt(i) <= 'z') {
				int c = (text.charAt(i) - key.charAt(keyProgress));
				if(c < 0) {
					result += (char)(c + 26 + 'a');
				}
				else {
					result += (char)((c % 26) + 'a');
				}
				keyProgress += 1;
				keyProgress = keyProgress % key.length();
			}
			else {
				result += text.charAt(i);
			}
		}

		return result;
	}

	//D�termine la longueur de la clef en v�rifiant, � chaque multiple de la p�riode, le caract�re observ�.
	//Si la distribution des caract�res correspond � la distribution normale de la langue anglaise (0.065), plus ou moins la tol�rance, on a trouv� la longueur de
	//la clef. Sinon, on incr�mente la p�riode de 1 et on recommence.
	public static int getKeySize(String text, double tolerance){
		int keySize = 0;
		int candidateKeySize = 0;

		text = text.toLowerCase();
		
		int periode = 1;
		double somme = 1;
		
		while(somme > 0.038) {
			double [] freq = new double[26];
			somme = 0;

			//Mesure des fr�quences des lettres � chaque periode p du text
			for (int i = 0; i*periode < text.length(); i++) {
				if(text.charAt(i*periode) >= 'a' && text.charAt(i*periode) <= 'z') {
					freq[text.charAt(i*periode)-'a']++;
				}
			}
			
			//Calcul de distribution
			for(int k = 0; k < freq.length; k++) {
				somme += Math.sqrt(freq[k]/text.length());
			}
			
			//V�rification si respecte la tol�rance
			if((somme < 0.065 + tolerance) && (somme > 0.065 - tolerance)) {
					keySize = periode;
					candidateKeySize++;
			}
			periode++;
		}
		System.out.println("Nombre de p�riode possible: " + candidateKeySize);
		return keySize;
	}

	//� partir de la longueur de la clef, il est possible de v�rifier la distribution des caract�res selon la valeur de d�calage pour chaque courant.
	//Si la distribution du caract�re dans le texte correspond � la distribution normale du caract�re dans la langue, on a trouv� le bon courant et on passe au prochain.
	public static String getKey(String text, int keySize){
		String result = "";

		//Fréquences théorique des lettres en anglais: f[0]=a, f[1]=b, etc.
		double[] f = new double[]{0.082,0.015,0.028,0.043,0.127,0.022,0.02,
			0.061,0.07,0.02,0.08,0.04,0.024,0.067,0.015,0.019,0.001,0.06,
			0.063,0.091,0.028,0.02,0.024,0.002,0.02,0.001};

		text = text.toLowerCase();
		
		int alphabet = 26;
		double [] freq_obs = new double[alphabet];
		double [] somme_table = new double [alphabet];
		
		for(int i = 0; i < keySize; i++) {
			
			somme_table = new double [alphabet];
			//On test chaque lettre de l'alphabet comme d�calage
			for(int k = 0; k < alphabet; k++) {
				
				freq_obs = new double[alphabet];
				for(int j = 0 ;i + (j *keySize) < text.length(); j++) {
					if(text.charAt(i+ (j *keySize)) >= 'a' && text.charAt(i+ (j *keySize)) <= 'z') {
						freq_obs[((text.charAt(i+ (j *keySize)) + k -'a') % alphabet)]++;
					}
				}
				
				double somme = 0;
				for(int j = 0; j < freq_obs.length; j++) {
					somme += f[j] * (keySize*freq_obs[j]/text.length());					
				}
				somme_table[k] = somme;
			}
			
			double closest_somme = Math.abs(somme_table[0]-0.065);
			int decalage = 0;
			for(int j = 1; j < somme_table.length; j++) {
				double diff = Math.abs(somme_table[j]-0.065);
				if(diff < closest_somme) {
					closest_somme = diff;
					decalage = j;
				}
			}
			
			result += (char)(decalage + 'a');
			
		}
		return result;
	}

	//Fonction principale.
	public static void main(String args[]){
		String text = "";
		
		try{
			text += readFile("cipher.txt", StandardCharsets.UTF_8);
		}catch(IOException e) {
			System.out.println("Can't load file");
		}

		double tolerance = 0.00002;

		int keySize = getKeySize(text, tolerance);
		System.out.println("Longueur de la clef : " + keySize);

		String key = getKey(text, keySize);
		System.out.println(key);

		text = decrypt(text, key);

		try (PrintWriter out = new PrintWriter("result.txt")) {
		    out.println(text);
		}catch(IOException e) {
			System.out.println("Can't write file");
		}
		
	}


}