package assignment_1;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

public class Crypto {

	/**
	 * @return true is {@code signature} is a valid digital signature of {@code message} under the key {@code pubKey}. 
	 * Internally, this uses RSA signature, but the student does not have to deal with
	 *         any of the implementation details of the specific signature algorithm
	 */
	public static boolean verifySignature(PublicKey pubKey, byte[] message, byte[] signature) {
		Signature sig = null;
		try {
			sig = Signature.getInstance("SHA256withRSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		try {
			sig.initVerify(pubKey);					// Checks the validity of the provided PublicKey
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		try {
			sig.update(message);					// Adds the provided message to the Signature object
			return sig.verify(signature);			// Verifies if the provided signature is valid for message using Key
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		return false;

	}
}
