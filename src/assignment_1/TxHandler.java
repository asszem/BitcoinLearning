package assignment_1;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;

public class TxHandler {

	UTXOPool utxoPool;														// create a copy of current utxoPool that is to be updated
	ArrayList<UTXO> claimedUTXOs;										    // List with all UTXO's that have been validated

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
		UTXO currentUTXO;																	// The UTXO that is claimed by a Transaction.Input
		ArrayList<UTXO> allUTXOs = utxoPool.getAllUTXO(); 						// Get all the UTXOs from current utxoPool

		// the claimedUTXO list needs to be reinstantiated with every call of isValidTX method. This list will be used in handleTX method
		claimedUTXOs = new ArrayList<>();

		// (1) all outputs claimed by {@code tx} are in the current UTXO pool

		// Check if every Input (outputs claimed by the transaction) in the transaction are referencing outputs that are in UTXO pool
		for (Transaction.Input underReviewInput : tx.getInputs()) {

			// Needs to be changed ONLY if tx found in UTXO pool
			isUTXOinUTXOPool = false;

			// Check if the output referenced by the current underReviewInput is in the UTXOPool
			for (UTXO utxoUnderReview : allUTXOs) {

				// Check if the UTXO under review references the same transaction hash as the Transaction.Input under review
				// TODO verify if this is valid, or equals() need to be used
				
				byte[] utxoHash=utxoUnderReview.getTxHash();
				byte[] inputReferencedHash=underReviewInput.prevTxHash;
				boolean isHashSame=Arrays.equals(utxoHash, inputReferencedHash);
				

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
						if (claimedUTXO.equals(currentUTXO)) {
							return false;
						}
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

	/**
	 * Handles each epoch by receiving an unordered array of proposed transactions, checking each transaction for correctness, returning a mutually valid array of accepted transactions, and updating
	 * the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {
		ArrayList<Transaction> acceptedTransactions = new ArrayList<>();

		for (Transaction transactionUnderReview : possibleTxs) {

			// check if tx is valid
			if (isValidTx(transactionUnderReview)) {
				// if TX was valid, then add tx to the acceptedTransactions arraylist
				acceptedTransactions.add(transactionUnderReview);

				// if TX was valid, then update the UTXOPool by removing all claimedUTXOs
				for (UTXO utxoToRemove : claimedUTXOs) {
					utxoPool.removeUTXO(utxoToRemove);
				} // end UTXO removal from pool
			}// end isValid(tx)
		} // end transactionUnderReview for-each cycle

		// Initialize acceptedTxs array with numAcceptedTxs
		Transaction[] acceptedTxs = new Transaction[acceptedTransactions.size()];

		// Copy values from ArrayList to Transaction[] array which is to be returned
		for (int i = 0; i < acceptedTransactions.size(); i++) {
			acceptedTxs[i] = acceptedTransactions.get(i);
		}

		return acceptedTxs;
	}

}
