/* Alexandre Dufour, p1054564
 * Jean-Fran�ois Blanchette, p1099987
 */

import java.util.*;


public class Differential{
	//Écrire votre numéro d'équipe içi !!!
	public static int teamNumber = 14;

	public static SPNServer server = new SPNServer();

	//Différentielle d'entrée \Delta_P
	//ex : ""0000 1011 0000 0000""
	public static String plain_diff = "0000101100000000";

	//Différentielle intermédiaire \Delta_I
	//ex : "0000011000000110"
	public static String int_diff = "0000011001100000";

	//Boîte à substitutions de l'exemple de la démonstration #3
	public static String[] sub_box_exemple = new String[]{"1110", "0100", "1101", "0001", "0010", "1111", "1011", "1000",
			   												"0011", "1010", "0110", "1100", "0101", "1001", "0000", "0111"};

	public static String[] sub_box_inv_exemple = new String[]{"1110", "0011", "0100", "1000", "0001", "1100", "1010", "1111", 
				   									  			"0111", "1101", "1001", "0110", "1011", "0010", "0000", "0101"};

	//Sorties des boîtes à substitutions du SPN
	public static String[] sub_box = new String[]{"1011", "0101", "0100", "1100", "0110", "0011", "1001", "1010",
												  "1101", "1111", "0001", "0000", "1110", "1000", "0111", "0010"};

	//Entrées des boîtes à substitutions du SPN
	public static String[] sub_box_inv = new String[]{"1011", "1010", "1111", "0101", "0010", "0001", "0100", "1110", 
				   									  "1101", "0110", "0111", "0000", "0011", "1000", "1100", "1001"};

	//Permutations : --> Notez que la permutation "perm" inverse est la même puisqu'elle est symmétrique
	public static int[] perm = new int[]{0, 4, 8, 12, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15};

	public static int[] pc1 = new int[]{15, 10, 5, 0, 16, 9, 7, 1, 17, 3, 19, 8, 6, 4, 18, 12, 14, 11, 13, 2};

	public static int[] pc1_inv = new int[]{3, 7, 19, 9, 13, 2, 12, 6, 11, 5, 1, 17, 15, 18, 16, 0, 4, 8, 14, 10};

	public static int[] pc2 = new int[]{9, 7, 0, 8, 5, 1, 4, 2, 16, 12, 19, 10, 17, 15, 13, 14};

	public static int[] pc2_inv = new int[]{2, 5, 7, 6, 4, 1, 3, 0, 11, 9, 14, 15, 13, 8, 12, 10};

	//Produit la table diff�rentielles des output pour chaque input entr�
	public static int[][] produceDiffTable(String[] sub){
		int[][] result = new int[16][16]; 

		for(int i = 0; i < 16; i++){
			int[] line = new int[16];

			//Pour chaque x'
			for(int j = 0; j < 16; j++){
				String y_p = "";
				String y_pp = "";
				
				String entree = Integer.toBinaryString(j + 0b10000).substring(1);
				y_p = sub(entree, sub);
						
				String x_pp = xor(entree, Integer.toBinaryString(i + 0b10000).substring(1));
				
				y_pp = sub(x_pp, sub);
				
				//D�terminer le delta_y
				String delta_y = xor(y_p, y_pp);
				
				//Comptabiliser la valeur de delta_y dans le tableau occurence
				line[Integer.parseInt(delta_y, 2)]++;
				
			}

			result[i] = line;
		}

		return result;
	}

	//Retroune 16 bits aléatoires en String
	public static String getRandomPlaintext(){
		String text = Integer.toBinaryString((int) Math.floor(Math.random()* 65536));

		while(text.length() != 16){
			text = "0" + text;
		}

		return text;
	}

	//Permute l'input en utilisant la permutation perm
	//À utiliser aussi avec pc1, pc1_inv, etc.
	public static String permute(String input, int[] perm){
		String output = "";

		for(int i = 0; i < perm.length; i++){
			output += input.charAt(perm[i]);
		}

		return output;
	}

	//Prend une entrée de 4 bits et retourne la valeur
	//associée dans l'argument sub_box
	public static String sub(String input, String[] sub_box){
		int value = 0;

		for(int i = 0; i < input.length(); i++){
			value <<= 1;

			if(input.charAt(i) == '1'){
				value += 1;
			}
		}

		return sub_box[value];
	}

	//Retourne l'input ayant fait "amount" rotation(s) vers la gauche
	public static String left_shift(String input, int amount){
		return input.substring(amount) + input.substring(0, amount);
	}

	//Retourne l'input ayant fait "amount" rotation(s) vers la droite
	public static String right_shift(String input, int amount){
		return input.substring(input.length() - amount) + input.substring(0, input.length() - amount);
	}

	//Retourne [k_1, k_2, k_3, k_4, k_5] calculées à partir  
	//de la clef maître "master" selon la génération de  
	//sous-clefs de la troisième démonstration
	public static String[] gen_keys(String master, int n){
		String[] result = new String[n];

		String pc1_res = permute(master, pc1);

		String left = pc1_res.substring(0,10);
		String right = pc1_res.substring(10);

		for(int i = 0; i < n; i++){
			int shift = i % 2 + 1;

			left = left_shift(left, shift);
			right = left_shift(right, shift);

			String temp = left + right;

			result[i] = permute(temp, pc2);
		}

		return result;
	}
	
	//Remont la chaine de k^5* jusqu'� la master key. On retrouve une cha�ne compos� de X, 1 et 0.
	public static String getPartialMasterkey(String partialSubkey, int n){
		
		String temp = permute(partialSubkey, pc2_inv);
		
		String temp1 = temp.substring(0,3)+"X";
		String temp2 = temp.substring(3,5)+"X";
		String temp3 = temp.substring(5,9)+"X";
		String temp4 = temp.substring(9,15)+"X";
		
		temp = temp1+temp2+temp3+temp4+temp.substring(15,16);
		
		String left = temp.substring(0,10);
		String right = temp.substring(10);
		
		//Retrouver la clef maître partielle grâce au résultat
		//de getPartialSubkey() en insérant des 'X' aux bits inconnues
		for(int i = n; i >= 0; i--){
			int shift = i % 2 + 1;

			left = right_shift(left,shift);
			right = right_shift(right,shift);

		}
		temp = left+right;

		return permute(temp,pc1_inv);
	}
	
	//Retourne un ou-exclusif entre les chaînes de caractères a et b
	public static String xor(String a, String b){
		if(a.length() != b.length()){
			return null;
		}
		String result = "";

		for(int i = 0; i < a.length(); i++){
			result += a.charAt(i) ^ b.charAt(i);
		}

		return result;
	}


	//Produit l'encryption de message avec la masterKey trouv�e.
	public static String encrypt(String plaintext, String[] subkeys){
		String cipher = plaintext;

		for(int i = 0; i < 4; i++){
			//sub-key mixing
			cipher = xor(cipher, subkeys[i]);

			//substitution
			cipher = rearrange(cipher, sub_box);

			//permutation
			cipher = permute(cipher, perm);
		}

		//Final sub-key mixing (5th sub-key)
		cipher = xor(cipher, subkeys[subkeys.length-1]);

		return cipher;
	}

	//d�termine la clef k^5* partielle � l'aide de 1000 paires de textes chiffr�s
	public static String getPartialSubkey(){
		int[] counts = new int[256];

		ArrayList<String> plaintexts = new ArrayList<>();

		for(int i = 0; i < 1000; i++){
			//Créaton de paires de messages clairs qui satisfont
			//la différentielle d'entrée \Delta_P

			String word_one = getRandomPlaintext();
			String word_two = xor(word_one, plain_diff);
			plaintexts.add(word_one);
			plaintexts.add(word_two);
			
		}

		//Encryption de ces messages clairs
		ArrayList<String> ciphers = server.encrypt(plaintexts,teamNumber);

		for(int j = 0; j < 256; j++){
			//Affectation du nombre de fois que chaque sous-clef partielle
			//j possible nous donne la différentielle intermédiaire 
			//"int_diff" à counts[j]
			
			String j_bin = String.format("%8s",  Integer.toBinaryString(j)).replace(' ', '0');
			String j_16bin = "0" + j_bin.charAt(0) + j_bin.charAt(1) + "0" + "0" + j_bin.charAt(2) + j_bin.charAt(3) + "0" +"0" +
							j_bin.charAt(4) + j_bin.charAt(5) + "0" + "0" + j_bin.charAt(6) + j_bin.charAt(7) + "0";
			
			ArrayList<String> ciphersPrime = new ArrayList<>();
			
			//Pour chaque cipher, nous faisons le xor avec la clef partielle, nous permutons et nous faisons la substitution inverse
			 for(int i = 0; i < ciphers.size(); i++) {
				 String xor_ciph_bin = xor(ciphers.get(i), j_16bin);
				 String permutaded = permute(xor_ciph_bin, perm);

				 String subed = rearrange(permutaded, sub_box_inv);
				 
				 ciphersPrime.add(subed);
			 }
			 
			 //Pour chaque pair de cipher, v�rifier si le xor donne int_diff. Si oui, incr�menter counts[j]
			 for(int i = 0; i < ciphers.size(); i++) {
				 if(int_diff.equals(xor(ciphersPrime.get(i), ciphersPrime.get(++i)))) {
					 counts[j] += 1;
				 }
			 }
		}

		//Déterminer la fréquence de clef la plus haute
		int max_count = counts[0];
		int index_count = 0;
		for(int i = 1; i < counts.length; i++) {
			if(counts[i] > max_count) {
				max_count = counts[i];
				index_count = i;
			}
		}

		//Déterminer la sous-clef k_5^* avec des 'X' au bits inconnues
		String  subkeyPartial= String.format("%8s",  Integer.toBinaryString(index_count)).replace(' ', '0');
		String subkey = "X" + subkeyPartial.charAt(0) + subkeyPartial.charAt(1) + "X" + "X" + subkeyPartial.charAt(2) + subkeyPartial.charAt(3) + "X" +"X" +
				subkeyPartial.charAt(4) + subkeyPartial.charAt(5) + "X" + "X" + subkeyPartial.charAt(6) + subkeyPartial.charAt(7) + "X";
		
		return subkey;
	}
	
	//Divise le texte en s�quence de 4 bits pour effectuer les substitions
	public static String rearrange(String in, String[] sub) {
		
		StringBuilder toConcat = new StringBuilder();
		
		for(int i = 4; i <= in.length(); i+=4) {
			String temp =in.substring(i-4, i);
			temp = sub(temp, sub);
			toConcat.append(temp);
		}
		return toConcat.toString();
	}

	//� partir de la masterKey partielle, test toute les possibilit�s pour les 12 bits restant. Pour chaque 
	//cl�e potentiel, on compare 100 textes al�atoires chiffr� par le serveur et chiffr� par notre cl�e pour voir si le r�sultat est le m�me.
	//Si dans un seul cas sur les 100 textes le r�sultat n'est pas le m�me, cette cl�e candidate est retir�.
	public static String bruteForce(String partialMasterkey){
		String result = "";
		boolean found = false;
		int repetition = 100;

		StringBuilder candidate = new StringBuilder(partialMasterkey);

		String iBin_12;


		for(int i = 0; i < 4096 && !found; i++){
			
			//Déterminer lesquelles des 2^12 (4096) possibilités de bits
			//manquantes donnent la bonne clef maître
			System.out.println("Candidat " + (i+1) + " sur 4096");
			
			iBin_12 = String.format("%12s",  Integer.toBinaryString(i)).replace(' ', '0');
			int k = 0;
			for (int j = 0; j<partialMasterkey.length();j++){
				if (partialMasterkey.charAt(j) == 'X') {
					candidate.setCharAt(j, iBin_12.charAt(k));
					k++;
				}
			}
			result = candidate.toString();
			
			String [] subkey = gen_keys(result, 5);
			
			found = true;
			
			//Test de plusieurs text al�atoire. Du moment qu'un encryptage par la clef candidate est diff�rente de celle encod� par 
			//le serveur, found = false et on essai une autre clef candidate.
			for(int j = 0; j < repetition && found; j++ ) {
				//Generating random plaintext
				String text = getRandomPlaintext();

				String res_server = server.encrypt(text,teamNumber);
				
				if(encrypt(text, subkey).equals(res_server)){
					found=true;
				}
				else {
					found = false;
					result = "";
				}
			}
			
			
		}
		System.out.println("Trouv�:" + found);

		//Retourner la clef maître
		return result;
	}

	//Fonction principale
	public static void main(String args[]){
		//Génération de la table des fréquences des différentielles de sortie
		//pour chaque différentielle d'entrée

		System.out.println(Arrays.deepToString(produceDiffTable(sub_box)));
		
		//Calcul de la sous-clef partielle k_5^*
		String partialSubkey = getPartialSubkey();
		System.out.println("Sous-clef partielle k_5^* : " + partialSubkey);
		
		//Calcul de la clef maître partielle k^* 
		String partialMasterkey = getPartialMasterkey(partialSubkey, 4);
		System.out.println("Clef maître partielle k^* : " + partialMasterkey);

		//Calcul de la clef maître par fouille exhaustive 
		String masterkey = bruteForce(partialMasterkey);
		System.out.println("Clef maître k : " + masterkey);
		
		//Information utile --> clef de l'exemple de la démo 3 : 00100100001111010101

		
	}

}