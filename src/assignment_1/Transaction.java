package assignment_1;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class Transaction {

	public class Input {

		/** hash of the Transaction whose output is being used */
		public byte[] prevTxHash;
		/** used output's index in the previous transaction */
		public int outputIndex;
		/** the signature produced to check validity */
		public byte[] signature;

		public Input(byte[] prevHash, int index) {
			if (prevHash == null)
				prevTxHash = null;
			else
				prevTxHash = Arrays.copyOf(prevHash, prevHash.length);
			outputIndex = index;
		}

		public void addSignature(byte[] sig) {
			if (sig == null)
				signature = null;
			else
				signature = Arrays.copyOf(sig, sig.length);
		}
	}

	public class Output {

		/** value in bitcoins of the output */
		public double value;
		/** the address or public key of the recipient */
		public PublicKey address;

		public Output(double v, PublicKey addr) {
			value = v;
			address = addr;
		}
	}

	/** hash of the transaction, its unique id */
	private byte[] hash;
	private ArrayList<Input> inputs;
	private ArrayList<Output> outputs;

	public Transaction() {
		inputs = new ArrayList<Input>();
		outputs = new ArrayList<Output>();
	}

	public Transaction(Transaction tx) {
		hash = tx.hash.clone();
		inputs = new ArrayList<Input>(tx.inputs);
		outputs = new ArrayList<Output>(tx.outputs);
	}

	public void addInput(byte[] prevTxHash, int outputIndex) {
		Input in = new Input(prevTxHash, outputIndex);
		inputs.add(in);
	}

	public void addOutput(double value, PublicKey address) {
		Output op = new Output(value, address);
		outputs.add(op);
	}

	public void removeInput(int index) {
		inputs.remove(index);
	}

	public void removeInput(UTXO ut) {
		for (int i = 0; i < inputs.size(); i++) {
			Input in = inputs.get(i);
			UTXO u = new UTXO(in.prevTxHash, in.outputIndex);
			if (u.equals(ut)) {
				inputs.remove(i);
				return;
			}
		}
	}

	// For the input to to be valid, the signature it contains must be a valid signature
	// over the current transaction with the public key in the spent output
	// the raw data that is signed is obtained from the getRawDataToSign method.
	// To verify a signature, use the verifySignature() method
	// verifySignature(PublicKey pubKey, byte[] message, byte[] signature)
	public byte[] getRawDataToSign(int index) {					// Index of input to be signed
		// with input and all outputs
		ArrayList<Byte> sigData = new ArrayList<Byte>();		// This will be the message to be signed

		// Convert the outputIndex and prevTXHash to bytes and add to the message to be signed
		if (index > inputs.size())								// Is input in the inputs array of current tx?
			return null;										// return null, nothing to sign
		Input in = inputs.get(index);							// Reference to the selected input object
		byte[] prevTxHash = in.prevTxHash;						// create byte array of input objects prevTx' hash
		ByteBuffer b = ByteBuffer.allocate(Integer.SIZE / 8);	// create a byte buffer for INT to store the Index
		b.putInt(in.outputIndex);								// Store the outputIndex number as bytes
		byte[] outputIndex = b.array();							// Create a byte array from bytebuffer
		if (prevTxHash != null) {								// If there was a previous transaction
			for (int i = 0; i < prevTxHash.length; i++) {		// Add every byte of prevTxHash to message
				sigData.add(prevTxHash[i]);
			}
		}
		for (int i = 0; i < outputIndex.length; i++) {			// Add every byte of outputIndex to message
			sigData.add(outputIndex[i]);
		}

		// Add EVERY output from the outputs ArrayList to the message that is to be signed...
		for (Output op : outputs) {								// Walk through all outputs in the current tx
			ByteBuffer bo = ByteBuffer.allocate(Double.SIZE / 8);	// Create bb to convert double value to bytes
			bo.putDouble(op.value);								// Add the output's value to the bb
			byte[] value = bo.array();							// Add content of bb to a byte[] array
			byte[] addressBytes = op.address.getEncoded();		// Add the output's PK to a byte[] array
			for (int i = 0; i < value.length; i++)
				sigData.add(value[i]);							// Append the value byte array to message

			for (int i = 0; i < addressBytes.length; i++)
				sigData.add(addressBytes[i]);					// Append the value of address to message
		} // end for
		byte[] sigD = new byte[sigData.size()];					// create a byte[] array from message arraylist
		int i = 0;
		for (Byte sb : sigData)									// go through every byte of message
			sigD[i++] = sb;										// add to the byte[] array
		return sigD;											// return the final byte array that is to be signed
	}

	public void addSignature(byte[] signature, int index) {
		inputs.get(index).addSignature(signature);
	}

	/* Creates a byte[] array with the content of
	 * All inputs with outputIndex, signature, prevTXhas
	 * All outputs with value, address
	 * 
	 */
	public byte[] getRawTx() {											// Get ALL inputs & Outputs in a byte[] array
		ArrayList<Byte> rawTx = new ArrayList<Byte>();
		for (Input in : inputs) {
			byte[] prevTxHash = in.prevTxHash;
			ByteBuffer b = ByteBuffer.allocate(Integer.SIZE / 8);
			b.putInt(in.outputIndex);
			byte[] outputIndex = b.array();
			byte[] signature = in.signature;
			if (prevTxHash != null)
				for (int i = 0; i < prevTxHash.length; i++)
					rawTx.add(prevTxHash[i]);
			for (int i = 0; i < outputIndex.length; i++)
				rawTx.add(outputIndex[i]);
			if (signature != null)
				for (int i = 0; i < signature.length; i++)
					rawTx.add(signature[i]);
		}
		for (Output op : outputs) {
			ByteBuffer b = ByteBuffer.allocate(Double.SIZE / 8);
			b.putDouble(op.value);
			byte[] value = b.array();
			byte[] addressBytes = op.address.getEncoded();
			for (int i = 0; i < value.length; i++) {
				rawTx.add(value[i]);
			}
			for (int i = 0; i < addressBytes.length; i++) {
				rawTx.add(addressBytes[i]);
			}

		}
		byte[] tx = new byte[rawTx.size()];
		int i = 0;
		for (Byte b : rawTx)
			tx[i++] = b;
		return tx;
	}

	public void finalize() {													// Creates a hash of the full Transaction (all inputs & outputs)
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(getRawTx());
			hash = md.digest();
		} catch (NoSuchAlgorithmException x) {
			x.printStackTrace(System.err);
		}
	}

	public void setHash(byte[] h) {
		hash = h;
	}

	public byte[] getHash() {
		return hash;
	}

	public ArrayList<Input> getInputs() {
		return inputs;
	}

	public ArrayList<Output> getOutputs() {
		return outputs;
	}

	public Input getInput(int index) {
		if (index < inputs.size()) {
			return inputs.get(index);
		}
		return null;
	}

	public Output getOutput(int index) {
		if (index < outputs.size()) {
			return outputs.get(index);
		}
		return null;
	}

	public int numInputs() {
		return inputs.size();
	}

	public int numOutputs() {
		return outputs.size();
	}
}
