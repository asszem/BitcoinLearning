package assignment_1;

import java.util.ArrayList;

public class TxHandler {

	UTXOPool utxoPool;

	/**
	 * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
	 * constructor.
	 */
	public TxHandler(UTXOPool utxoPool) {
		// IMPLEMENT THIS
		this.utxoPool = new UTXOPool(utxoPool);
	}

	/**
	 * @return true if: (1) all outputs claimed by {@code tx} are in the current UTXO pool, (2) the signatures on each input of {@code tx} are valid, (3) no UTXO is claimed multiple times by
	 *         {@code tx}, (4) all of {@code tx}s output values are non-negative, and (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output values; and false
	 *         otherwise.
	 */
	public boolean isValidTx(Transaction tx) {
		// (1) all outputs claimed by {@code tx} are in the current UTXO pool
		if (!isAllClaimedOutputsInUTXOPool(tx))
			return false;

		// This can be reached only if all output is valid

		// (2) the signatures on each input of {@code tx} are valid,
		if (!isAllSignaturesOnEachInputValid(tx))
			return false;

		// Only get here if everything passed
		return true;
	}

	/*
	 * Return true if all Transaction.Output in {@code tx} is in the current UTXOPool
	 * 
	 */
	public boolean isAllClaimedOutputsInUTXOPool(Transaction tx) {
		int currentTxOutputIndex = 0;														// Keep track of index in argument tx
		boolean isTXinUTXOPool = false;														// This must be set to True for each tx.Output
		for (Transaction.Output output : tx.getOutputs()) {									// Walk through each Transaction.Outputs in argument tx
			ArrayList<UTXO> allUTXO = utxoPool.getAllUTXO(); 								// Get all the UTXOs from current utxoPool
			for (UTXO utxo : allUTXO) {														// Check each UTXO
				// is the txHash in UTXO same as the hash of tx that is to be verified?
				// TODO verify if this is valid, or equals() need to be used
				boolean isHashSame = utxo.getTxHash() == tx.getHash();						// Does UTXO hash refer tx's hash?
				if (!isHashSame)
					continue;																// If not, then UTXO is different continue with next UTXO for-each cylce
				// is OutputIndex the same in UTXO and current output?
				boolean isOutputIndexSame = utxo.getIndex() == currentTxOutputIndex;
				if (isOutputIndexSame) {
					isTXinUTXOPool = true;
					break;																	// Break from the UTXO for-each cycle, as output is validated
				} // end if
			} // end UTXO for-each cycle

			// This is reached when all UTXO's have been checked. If currentTxOutput was not found then isValidTX returns false
			if (!isTXinUTXOPool) {
				return false;																// If an output is NOT in the pool, tx is NOT valid
			}

			// Get ready for the next transaction.output verification cycle
			isTXinUTXOPool = false;															// Set the verification boolean to false
			currentTxOutputIndex++;															// Increment currentTXOutputIndex
		} // end output for

		// This can be reached only if all outputs tested positive
		return true;
	}

	/*
	 * Return true if the signatures on each input of {@code tx} are valid
	 */
	public boolean isAllSignaturesOnEachInputValid(Transaction tx) {

		int inputIndex = 0;
		for (Transaction.Input input : tx.getInputs()) {
			// Return false if any of the input fails
			int outputIndex = input.outputIndex;
			byte[] prevTXHash = input.prevTxHash;
			byte[] signature = input.signature;

			// TODO find out how to get the PublicKey for verification
			// I can't reference the previous TX OutPut inner class to get the PK stored in it...
			if (!Crypto.verifySignature(tx.getOutput(inputIndex).address, tx.getRawDataToSign(inputIndex),
					input.signature)) {
				return false;
			}
			inputIndex++;
		}

		return true;
	}

	/**
	 * Handles each epoch by receiving an unordered array of proposed transactions, checking each transaction for correctness, returning a mutually valid array of accepted transactions, and updating
	 * the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {
		// IMPLEMENT THIS
		Transaction[] acceptedTxs;
		int numAcceptedTxs = 0;
		for (Transaction transaction : possibleTxs) {
			// if valid - add to the result array

			// if valid - update the utxo

			// if valid - increment numAcceptedTxs
		}
		// Initialize acceptedTxs array with numAcceptedTxs
		acceptedTxs = new Transaction[numAcceptedTxs];
		return acceptedTxs;
	}

}
