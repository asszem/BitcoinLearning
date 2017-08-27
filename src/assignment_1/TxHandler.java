package assignment_1;

import java.security.PublicKey;
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
		int totalInputValue = 0;
		int totalOutputValue = 0;
		int indexOfInputUnderReview = 0;
		boolean isUTXOinUTXOPool;
		ArrayList<UTXO> allUTXOs = utxoPool.getAllUTXO(); 									// Get all the UTXOs from current utxoPool
		ArrayList<UTXO> claimedUTXOs = new ArrayList<>();									// List with all UTXO's that have been validated
		UTXO currentUTXO;																	// The UTXO that is claimed by a Transaction.Input

		// (1) all outputs claimed by {@code tx} are in the current UTXO pool

		// Check if every Input (outputs claimed by the transaction) in the transaction are referencing outputs that are in UTXO pool
		for (Transaction.Input underReviewInput : tx.getInputs()) {

			// Needs to be changed ONLY if tx found in UTXO pool
			isUTXOinUTXOPool = false;

			// Check if the output referenced by the current underReviewInput is in the UTXOPool
			for (UTXO utxoUnderReview : allUTXOs) {

				// Check if the UTXO under review references the same transaction hash as the Transaction.Input under review
				// TODO verify if this is valid, or equals() need to be used
				boolean isHashSame = utxoUnderReview.getTxHash() == underReviewInput.prevTxHash;	// If the UTXO references the same hash as the reviewed input

				// If not, then UTXO is different continue with next UTXO for-each cylce
				if (!isHashSame) {
					continue;
				}

				// Check if the UTXO under review has the same output index as the Transaction.Input under review
				boolean isOutputIndexSame = utxoUnderReview.getIndex() == underReviewInput.outputIndex;

				if (isOutputIndexSame) {

					// At this point we found the UTXO that was referenced by the underReviewInput
					// If any of the further validations fails in this block, the isValidTx will return false

					// Set the boolean to true so that execution can continue outside the UTXO for-each block
					isUTXOinUTXOPool = true;

					// set the currentUTXO to reference the found UTXO object
					currentUTXO = utxoUnderReview;

					// METHOD FAILING VALIDATION
					// check that (3) no UTXO is claimed multiple times by {@code tx}
					for (UTXO claimedUTXO : claimedUTXOs) {

						// if the currentUTXO is already in the claimedUTXO list then this is a doubleSpend then the isValidTx must return false
						if (claimedUTXO.equals(currentUTXO))
							return false;
					}

					// METHOD FAILING VALIDATION
					// (2) the signatures on each input of {@code tx} are valid
					byte[] signature = underReviewInput.signature;
					// TODO verify if this is the correct call. Maybe the rawData should be referencing the originating Tx?
					byte[] message = tx.getRawDataToSign(indexOfInputUnderReview);
					PublicKey pubKey = utxoPool.getTxOutput(utxoUnderReview).address;
					if (!Crypto.verifySignature(pubKey, message, signature)) {
						return false;
					}

					// This can be reached only if UTXO was not found in the list of claimedUTXOs, it's signature is validated so we add it to the list
					claimedUTXOs.add(currentUTXO);

					// Add the value of UTXO's Output to the total value of inputs. This can be done only if everything else is fine with the input
					totalInputValue += utxoPool.getTxOutput(currentUTXO).value;
					break;																	// Break from the UTXO for-each cycle
				} // end if
			} // end UTXO for-each cycle

			// This is reached when all UTXO's have been checked. If currentTxOutput was not found then isValidTX returns false
			if (!isUTXOinUTXOPool) {
				return false;																// If an output is NOT in the pool, tx is NOT valid
			}

			// This can be reached only if UTXO is in the UTXOPool and signature is valid

			// Get ready to continue with the review of next Transaction.Input

			// Set the verification boolean to false
			isUTXOinUTXOPool = false;

			// increment the index of input under review
			indexOfInputUnderReview++;

		} // end of underReviewInput for-each cycle

		// If this point is reached, then all inputs are verified

		// (4) all of {@code tx}s output values are non-negative,
		for (Transaction.Output output : tx.getOutputs()) {
			if (output.value < 0) {
				return false;
			}
		}

		// (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output values;
		if (totalInputValue < totalOutputValue)
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
		for (Transaction.Input input : tx.getInputs()) {						// Walk through all inputs in argument tx

			if (!Crypto.verifySignature(tx.getOutput(inputIndex).address, tx.getRawDataToSign(inputIndex),
					input.signature)) {
				return false;
			}
			inputIndex++;
		}

		// This can be reached only if all signatures are valid
		return true;
	}

	public int sumOfInputValues(Transaction tx){
		int sum=0;
		int currentInputValue=0;
		for (Transaction.Input input : tx.getInputs()){
			input.outputIndex;
		}
		
		
		
		
		return sum;
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
