import request from "@/api/request";

// Common pagination params
export interface PageParams {
  page: number;
  size: number;
}

export interface PageResult<T> {
  total: number;
  records: T[];
}

// Holder & Card core types
export type HolderType = "STUDENT" | "TEACHER" | "STAFF" | "VISITOR";

export interface CardType {
  id: number;
  name: string;
  description?: string;
}

export interface Card {
  id: number;
  cardNo: string;
  typeId: number;
  holderType: HolderType;
  holderId: string;
  status: "ACTIVE" | "LOST" | "FROZEN" | "CANCELLED";
  balance: number;
  createdAt?: string;
}

// Issue / Loss types
export interface IssueReq {
  holderType: HolderType;
  holderId: string;
  cardTypeId: number;
  initialBalance?: number;
  note?: string;
  expireAt?: string; // 仅临时卡
}

export interface LossReq {
  cardNo: string;
  reason?: string;
}

// Balance & Recharge types
export interface BalanceInfo {
  cardNo: string;
  holderName?: string;
  cardTypeName?: string;
  status?: "ACTIVE" | "LOST" | "FROZEN" | "CANCELLED";
  balance: number;
  updatedAt?: string;
}

export type RechargeMethod = "CASH" | "ONLINE" | "ALIPAY" | "WECHAT" | "CARD_CENTER";

export interface RechargeReq {
  cardNo: string;
  amount: number; // positive number
  method?: RechargeMethod;
  note?: string;
}

// Refund & Freeze
export interface RefundReq {
  cardNo: string;
  amount: number;
  method?: RechargeMethod;
  note?: string;
}

export interface FreezeReq {
  cardNo: string;
  reason?: string;
}

export interface UnfreezeReq {
  cardNo: string;
}

// Cancel & Replace
export interface CancelReq {
  cardNo: string;
  refundAll?: boolean; // 是否退回剩余余额
  method?: RechargeMethod; // 退款方式（若refundAll=true）
  note?: string;
}

export interface ReplaceReq {
  oldCardNo: string;
  changeType?: "SUPPLEMENT" | "TYPE_CHANGE"; // 补卡或换卡
  newCardTypeId?: number; // 换卡时可选择新卡类型
  fee?: number; // 工本费/换卡费
  note?: string;
}

// Transactions
export type TransactionType = "CONSUME" | "RECHARGE" | "REFUND";

export interface TransactionRecord {
  id: number;
  cardNo: string;
  type: TransactionType;
  amount: number; // 消费为正数表示支出，充值/退款为正数表示收入
  balanceAfter: number;
  merchant?: string;
  occurredAt: string;
  note?: string;
}

export interface TransQuery extends Partial<PageParams> {
  cardNo?: string;
  type?: TransactionType;
  start?: string; // ISO date-time string
  end?: string;   // ISO date-time string
}

// ------- API Calls -------

export async function listCardTypes(): Promise<CardType[]> {
  const res = await request.get("/api/v1/cards/types");
  return res || [];
}

export async function createCardType(data: Pick<CardType, "name" | "description">): Promise<{ success: boolean; id?: number }>{
  try {
    const res = await request.post("/api/v1/cards/types", data);
    return { success: true, id: (res as any)?.id };
  } catch (e) {
    return { success: false };
  }
}

export async function updateCardType(id: number, data: Pick<CardType, "name" | "description">): Promise<{ success: boolean }>{
  try {
    await request.put(`/api/v1/cards/types/${id}`, data);
    return { success: true };
  } catch (e) {
    return { success: false };
  }
}

export async function deleteCardType(id: number): Promise<{ success: boolean }>{
  try {
    await request.delete(`/api/v1/cards/types/${id}`);
    return { success: true };
  } catch (e) {
    return { success: false };
  }
}

export async function listCards(params: Partial<PageParams> & Partial<Card>): Promise<PageResult<Card>> {
  const res = await request.get("/api/v1/cards", { params });
  return (res as any) || { total: 0, records: [] };
}

export async function listTransactions(params: TransQuery): Promise<PageResult<TransactionRecord>> {
  const { cardNo, type, start, end, page = 1, size = 10 } = params;
  if (!cardNo) {
    return { total: 0, records: [] };
  }
  const query: any = {};
  if (type) query.type = type;
  if (start) query.startTime = start;
  if (end) query.endTime = end;
  try {
    const list = await request.get(`/api/v1/cards/${cardNo}/transactions`, { params: query }) as TransactionRecord[];
    const total = Array.isArray(list) ? list.length : 0;
    const from = Math.max(0, (page - 1) * size);
    const to = Math.min(total, from + size);
    const records = Array.isArray(list) ? list.slice(from, to) : [];
    return { total, records };
  } catch (e) {
    return { total: 0, records: [] };
  }
}

export async function issueCard(data: IssueReq): Promise<{ success: boolean; cardNo?: string }>{
  try {
    const payload: any = {
      typeId: (data as any).cardTypeId,
      holderType: data.holderType,
      holderId: String((data as any).holderId),
      initialBalance: data.initialBalance,
      note: data.note
    };
    if (data.holderType === 'VISITOR' && data.expireAt) payload.expireAt = data.expireAt;
    const res = await request.post("/api/v1/cards/issue", payload);
    return { success: true, cardNo: (res as any)?.cardNo };
  } catch (e) {
    return { success: false };
  }
}

export async function reportLoss(data: LossReq): Promise<{ success: boolean }>{
  try {
    await request.post("/api/v1/cards/loss", data);
    return { success: true };
  } catch (e) {
    return { success: false };
  }
}

export async function unloss(data: LossReq): Promise<{ success: boolean }>{
  try {
    await request.post("/api/v1/cards/unloss", data);
    return { success: true };
  } catch (e) {
    return { success: false };
  }
}

export async function getBalance(cardNo: string): Promise<BalanceInfo> {
  const res = await request.get(`/api/v1/cards/${cardNo}/balance`);
  return (res as any);
}

export async function recharge(data: RechargeReq): Promise<{ success: boolean; balance?: number }>{
  try {
    const res = await request.post("/api/v1/cards/recharge", data);
    return (res as any) || { success: true };
  } catch (e) {
    return { success: false };
  }
}

export async function refund(data: RefundReq): Promise<{ success: boolean; balance?: number }>{
  try {
    const res = await request.post("/api/v1/cards/refund", data);
    return (res as any) || { success: true };
  } catch (e) {
    return { success: false };
  }
}

export async function freeze(data: FreezeReq): Promise<{ success: boolean }>{
  try {
    const res = await request.post("/api/v1/cards/freeze", data);
    return (res as any) || { success: true };
  } catch (e) {
    return { success: false };
  }
}

export async function unfreeze(data: UnfreezeReq): Promise<{ success: boolean }>{
  try {
    const res = await request.post("/api/v1/cards/unfreeze", data);
    return (res as any) || { success: true };
  } catch (e) {
    return { success: false };
  }
}

export async function cancelCard(data: CancelReq): Promise<{ success: boolean }>{
  try {
    // 后端字段为 refund(boolean) 与 note，未使用 method
    const payload: any = { cardNo: data.cardNo, refund: !!data.refundAll, note: data.note };
    await request.post("/api/v1/cards/cancel", payload);
    return { success: true };
  } catch (e) {
    return { success: false };
  }
}

export async function replaceCard(data: ReplaceReq): Promise<{ success: boolean; newCardNo?: string }>{
  try {
    const res = await request.post("/api/v1/cards/replace", data);
    return (res as any) || { success: true };
  } catch (e) {
    return { success: false };
  }
}

export async function batchIssue(list: IssueReq[]): Promise<{ success: boolean; count?: number; issued?: string[] }>{
  try {
    const payload = list.map((d) => ({
      typeId: (d as any).cardTypeId,
      holderType: d.holderType,
      holderId: String((d as any).holderId),
      initialBalance: d.initialBalance,
      note: d.note,
      expireAt: (d.holderType === 'VISITOR' ? d.expireAt : undefined)
    }));
    // 后端批量发卡入参为 { items: IssueCardReq[] }
    const res = await request.post("/api/v1/cards/issue/batch", { items: payload });
    // 响应为 { success, count, cardNos }，兼容此前的 issued 字段
    const issued = (res as any)?.cardNos || (res as any)?.issued || [];
    return { success: true, count: issued.length, issued };
  } catch (e) {
    return { success: false };
  }
}

export async function consume(data: { cardNo: string; amount: number; merchant?: string; note?: string }): Promise<{ success: boolean; balance?: number }>{
  try {
    const res = await request.post("/api/v1/cards/consume", data);
    return (res as any) || { success: true };
  } catch (e) {
    return { success: false };
  }
}