/*
 * Copyright the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.schildbach.wallet.util;

import de.schildbach.wallet.Constants;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.SegwitAddress;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletTransaction;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Andreas Schildbach
 */
public class WalletUtilsTest {
    @Test
    public void restoreWalletFromProtobufOrBase58() throws Exception {
        WalletUtils.restoreWalletFromProtobuf(getClass().getResourceAsStream("backup-protobuf-testnet"),
                TestNet3Params.get());
    }

    @Test(expected = IOException.class)
    public void restoreWalletFromProtobuf_wrongNetwork() throws Exception {
        WalletUtils.restoreWalletFromProtobuf(getClass().getResourceAsStream("backup-protobuf-testnet"),
                MainNetParams.get());
    }

    @Test
    public void importedKeysWithBalance() {
        final Wallet wallet = Wallet.createBasic(Constants.NETWORK_PARAMETERS);
        final ECKey fundedKey = new ECKey();
        final ECKey emptyKey = new ECKey();
        wallet.importKey(fundedKey);
        wallet.importKey(emptyKey);

        final Transaction legacyFunding = new Transaction(Constants.NETWORK_PARAMETERS);
        legacyFunding.addOutput(Coin.CENT, LegacyAddress.fromKey(Constants.NETWORK_PARAMETERS, fundedKey));
        wallet.addWalletTransaction(new WalletTransaction(WalletTransaction.Pool.UNSPENT, legacyFunding));

        final Transaction segwitFunding = new Transaction(Constants.NETWORK_PARAMETERS);
        segwitFunding.addOutput(Coin.MILLICOIN, ScriptBuilder.createP2WPKHOutputScript(
                SegwitAddress.fromKey(Constants.NETWORK_PARAMETERS, fundedKey).getHash()));
        wallet.addWalletTransaction(new WalletTransaction(WalletTransaction.Pool.UNSPENT, segwitFunding));

        assertEquals(Coin.CENT.add(Coin.MILLICOIN), WalletUtils.getBalance(wallet, fundedKey));
        assertEquals(Coin.ZERO, WalletUtils.getBalance(wallet, emptyKey));
        assertEquals(1, WalletUtils.getImportedKeysWithBalance(wallet).size());
        assertTrue(WalletUtils.getImportedKeysWithBalance(wallet).contains(fundedKey));
        assertTrue(WalletUtils.hasImportedKeysWithBalance(wallet));
    }

    @Test
    public void importedKeysWithoutBalance() {
        final Wallet wallet = Wallet.createBasic(Constants.NETWORK_PARAMETERS);
        wallet.importKey(new ECKey());

        assertFalse(WalletUtils.hasImportedKeysWithBalance(wallet));
    }
}
