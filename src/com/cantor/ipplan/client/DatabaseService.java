/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.cantor.ipplan.client;

import java.util.Date;
import java.util.List;

import com.cantor.ipplan.shared.BargainTotals;
import com.cantor.ipplan.shared.BargainWrapper;
import com.cantor.ipplan.shared.CostsWrapper;
import com.cantor.ipplan.shared.CustomerWrapper;
import com.cantor.ipplan.shared.ImportProcessInfo;
import com.cantor.ipplan.shared.PUserWrapper;
import com.cantor.ipplan.shared.StatusWrapper;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("database")
public interface DatabaseService extends RemoteService {
	public PUserWrapper open(String sessId) throws Exception;
	public PUserWrapper isLogged();
	public List<BargainWrapper> attention() throws Exception;
	public BargainTotals[] getTotals() throws Exception;
	public BargainWrapper newBargain(String name, int startStatus) throws Exception;
	public BargainWrapper newBargain(String name, int startStatus, Date start, Date finish) throws Exception;
	public BargainWrapper editBargain(int id) throws Exception;
	public boolean deleteBargain(int id) throws Exception;
	public List<BargainWrapper> getTemporalyBargains() throws Exception;
	public void dropTemporalyBargain(int id);
	public void saveTemporalyBargain(BargainWrapper bargain) throws Exception;
	public BargainWrapper saveBargain(BargainWrapper bargain, boolean drop) throws Exception;
	public List<CustomerWrapper> findCustomer(String query);
	public List<StatusWrapper> getAllStatuses();
	public List<CostsWrapper> findCost(String newtext);
	public void refreshGoogleToken() throws Exception;
	public ImportProcessInfo syncContacts() throws Exception;
	public void setContactsAutoSync(int duration) throws Exception;
	public void setCalendarAutoSync(int duration) throws Exception;
	public ImportProcessInfo syncCalendar() throws Exception;
}
