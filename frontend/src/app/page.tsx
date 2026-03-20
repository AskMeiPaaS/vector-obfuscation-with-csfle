"use client";

import { useState, useRef, useEffect } from "react";
import { useLogStream } from "@/hooks/useLogStream";
import { Terminal, Activity, ArrowRight, ShieldCheck, RefreshCw, CreditCard, Search, Coffee, ShoppingBag, Tv, History, Lock, Grid3X3, Database } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";

type Transaction = {
  id: string;
  merchant: string;
  amount: number;
  description: string;
  timestamp: string;
  obfuscatedVector: number[];
};

const PREPOPULATED_OPTIONS = [
  { merchant: "Starbucks", description: "Morning Coffee", icon: Coffee, amount: 5.50 },
  { merchant: "Amazon", description: "Online Shopping", icon: ShoppingBag, amount: 45.99 },
  { merchant: "Netflix", description: "Monthly Subscription", icon: Tv, amount: 15.99 },
];

export default function Home() {
  const [activeTab, setActiveTab] = useState<"upi" | "search">("upi");
  
  // UPI Form State
  const [merchant, setMerchant] = useState("");
  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  const [isProcessing, setIsProcessing] = useState(false);
  const [lastTransaction, setLastTransaction] = useState<Transaction | null>(null);

  // Search State
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState<Transaction[]>([]);
  const [isSearching, setIsSearching] = useState(false);

  const { logs, isConnected } = useLogStream();
  const terminalEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    terminalEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [logs]);

  const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api/v1";

  const handlePay = async () => {
    if (!merchant || !amount || !description) {
      alert("Please fill in all fields.");
      return;
    }

    setIsProcessing(true);
    setLastTransaction(null);

    try {
      const response = await fetch(`${apiUrl}/transactions`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ merchant, amount: parseFloat(amount), description }),
      });

      if (!response.ok) throw new Error("Payment failed");

      const data = await response.json();
      setLastTransaction(data);
      
      // Reset form
      setMerchant("");
      setAmount("");
      setDescription("");
    } catch (error) {
      console.error("Error during payment:", error);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleSearch = async () => {
    if (!searchQuery) return;

    setSearchResults([]); // Cleanup historical results
    setIsSearching(true);
    try {
      const response = await fetch(`${apiUrl}/transactions/search?query=${encodeURIComponent(searchQuery)}`);
      if (!response.ok) throw new Error("Search failed");
      const data = await response.json();
      setSearchResults(data);
    } catch (error) {
      console.error("Error during search:", error);
    } finally {
      setIsSearching(false);
    }
  };

  const prepopulate = (option: typeof PREPOPULATED_OPTIONS[0]) => {
    setMerchant(option.merchant);
    setAmount(option.amount.toString());
    setDescription(option.description);
  };

  return (
    <main className="min-h-screen bg-neutral-950 text-neutral-200 p-4 md:p-8 font-sans selection:bg-emerald-500/30">
      <div className="max-w-6xl mx-auto space-y-8">

        {/* Header */}
        <motion.header 
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="border-b border-neutral-800 pb-6 flex flex-col md:flex-row md:items-end justify-between gap-4"
        >
          <div>
            <div className="flex items-center gap-3">
              <div className="p-2 bg-emerald-500/10 rounded-lg border border-emerald-500/20">
                <ShieldCheck className="w-7 h-7 text-emerald-500" />
              </div>
              <div>
                <h1 className="text-2xl md:text-3xl font-bold text-white tracking-tight leading-tight">Securing Vectors with Orthogonal Matrix</h1>
                <h2 className="text-lg text-emerald-400 font-medium">& MongoDB CSFLE</h2>
              </div>
            </div>
            <div className="flex flex-wrap gap-2 mt-4">
              <span className="flex items-center gap-1.5 text-xs bg-neutral-900 border border-neutral-700 px-3 py-1.5 rounded-full text-neutral-400">
                <Grid3X3 className="w-3 h-3 text-emerald-500" /> Orthogonal Matrix Obfuscation
              </span>
              <span className="flex items-center gap-1.5 text-xs bg-neutral-900 border border-neutral-700 px-3 py-1.5 rounded-full text-neutral-400">
                <Lock className="w-3 h-3 text-amber-500" /> CSFLE Encrypted at Rest
              </span>
              <span className="flex items-center gap-1.5 text-xs bg-neutral-900 border border-neutral-700 px-3 py-1.5 rounded-full text-neutral-400">
                <Database className="w-3 h-3 text-blue-500" /> Distance-Preserving Search
              </span>
            </div>
          </div>
          <div className={`flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-medium border ${
            isConnected ? 'bg-emerald-500/10 border-emerald-500/20 text-emerald-400' : 'bg-red-500/10 border-red-500/20 text-red-400'
          }`}>
            <div className={`w-2 h-2 rounded-full ${isConnected ? 'bg-emerald-500 animate-pulse' : 'bg-red-500'}`} />
            {isConnected ? 'LIVE STREAM CONNECTED' : 'DISCONNECTED'}
          </div>
        </motion.header>

        {/* Tabs Navigation */}
        <div className="flex gap-1 bg-neutral-900/50 p-1 rounded-xl border border-neutral-800 w-fit">
          <button
            onClick={() => setActiveTab("upi")}
            className={`flex items-center gap-2 px-6 py-2.5 rounded-lg text-sm font-medium transition-all ${
              activeTab === "upi" ? "bg-emerald-600 text-white shadow-lg" : "text-neutral-400 hover:text-white hover:bg-neutral-800"
            }`}
          >
            <CreditCard className="w-4 h-4" /> Create Transaction
          </button>
          <button
            onClick={() => setActiveTab("search")}
            className={`flex items-center gap-2 px-6 py-2.5 rounded-lg text-sm font-medium transition-all ${
              activeTab === "search" ? "bg-emerald-600 text-white shadow-lg" : "text-neutral-400 hover:text-white hover:bg-neutral-800"
            }`}
          >
            <Search className="w-4 h-4" /> Vector Search
          </button>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          
          {/* Main Workspace */}
          <div className="space-y-6">
            <AnimatePresence mode="wait">
              {activeTab === "upi" ? (
                <motion.div
                  key="upi-tab"
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: 20 }}
                  className="space-y-6"
                >
                  <div className="bg-neutral-900 border border-neutral-800 rounded-xl p-6">
                    <h2 className="text-xl font-semibold text-white mb-2 flex items-center gap-2">
                       <CreditCard className="w-5 h-5 text-emerald-500" /> Create Transaction
                    </h2>
                    <p className="text-xs text-neutral-500 mb-4">Text is embedded via Ollama → obfuscated with orthogonal matrix → encrypted via CSFLE → stored in MongoDB.</p>
                    <div className="flex flex-wrap gap-3 mb-6">
                      {PREPOPULATED_OPTIONS.map((opt, i) => (
                        <button
                          key={i}
                          onClick={() => prepopulate(opt)}
                          className="flex items-center gap-2 bg-neutral-950 border border-neutral-700 hover:border-emerald-500 px-4 py-2 rounded-lg text-sm transition-all group"
                        >
                          <opt.icon className="w-4 h-4 text-emerald-500 group-hover:scale-110 transition-transform" />
                          {opt.merchant}
                        </button>
                      ))}
                    </div>

                    <div className="space-y-4">
                      <div>
                        <label className="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-1.5 ml-1">Merchant</label>
                        <input
                          type="text"
                          value={merchant}
                          onChange={(e) => setMerchant(e.target.value)}
                          className="w-full bg-neutral-950 border border-neutral-700 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-emerald-500 transition-all"
                          placeholder="e.g., Starbucks"
                        />
                      </div>
                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <label className="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-1.5 ml-1">Amount ($)</label>
                          <input
                            type="number"
                            value={amount}
                            onChange={(e) => setAmount(e.target.value)}
                            className="w-full bg-neutral-950 border border-neutral-700 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-emerald-500 transition-all font-mono"
                            placeholder="0.00"
                          />
                        </div>
                        <div>
                          <label className="block text-xs font-bold text-neutral-500 uppercase tracking-widest mb-1.5 ml-1">Description</label>
                          <input
                            type="text"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            className="w-full bg-neutral-950 border border-neutral-700 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-emerald-500 transition-all"
                            placeholder="e.g., Coffee"
                          />
                        </div>
                      </div>
                      <button
                        onClick={handlePay}
                        disabled={isProcessing}
                        className="w-full bg-emerald-600 hover:bg-emerald-500 text-white py-3 rounded-lg font-bold transition-all disabled:opacity-50 flex items-center justify-center gap-2 shadow-lg shadow-emerald-900/20 mt-2"
                      >
                        {isProcessing ? <RefreshCw className="w-5 h-5 animate-spin" /> : <CreditCard className="w-5 h-5" />}
                        {isProcessing ? "EMBEDDING & OBFUSCATING..." : "EMBED → OBFUSCATE → ENCRYPT → STORE"}
                      </button>
                    </div>
                  </div>

                  {lastTransaction && (
                    <motion.div 
                      initial={{ opacity: 0, scale: 0.95 }}
                      animate={{ opacity: 1, scale: 1 }}
                      className="bg-neutral-900 border border-emerald-900/50 rounded-xl p-6 shadow-2xl relative overflow-hidden"
                    >
                      <div className="absolute top-0 right-0 p-3 opacity-10">
                        <ShieldCheck className="w-24 h-24 text-emerald-500" />
                      </div>
                      <h3 className="text-emerald-400 font-bold flex items-center gap-2 mb-4">
                        <History className="w-4 h-4" /> Last Transaction (Obfuscated)
                      </h3>
                      <div className="space-y-3 relative z-10">
                        <div className="flex justify-between border-b border-neutral-800 pb-2">
                          <span className="text-neutral-500 text-sm">Merchant</span>
                          <span className="text-white font-medium">{lastTransaction.merchant}</span>
                        </div>
                        <div className="flex justify-between border-b border-neutral-800 pb-2">
                          <span className="text-neutral-500 text-sm">Amount</span>
                          <span className="text-white font-mono">${lastTransaction.amount.toFixed(2)}</span>
                        </div>
                        <div className="space-y-1">
                          <span className="text-neutral-500 text-sm">Obfuscated Vector <span className="text-amber-500 text-xs">(Matrix encrypted via CSFLE)</span></span>
                          <div className="bg-neutral-950 p-3 rounded border border-neutral-800 font-mono text-[10px] text-emerald-500/80 break-all">
                            [{lastTransaction.obfuscatedVector.map(v => v.toFixed(4)).join(", ")}]
                          </div>
                        </div>
                      </div>
                    </motion.div>
                  )}
                </motion.div>
              ) : (
                <motion.div
                  key="search-tab"
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: 20 }}
                  className="space-y-6"
                >
                  <div className="bg-neutral-900 border border-neutral-800 rounded-xl p-6">
                    <h2 className="text-xl font-semibold text-white mb-2 flex items-center gap-2">
                      <Search className="w-5 h-5 text-emerald-500" /> Semantic Vector Search
                    </h2>
                    <p className="text-xs text-neutral-500 mb-6">
                      Query is embedded → obfuscated with same matrix → cosine similarity computed on obfuscated space → results ranked by relevancy score.
                    </p>
                    <div className="flex gap-3">
                      <div className="relative flex-1">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-neutral-500" />
                        <input
                          type="text"
                          value={searchQuery}
                          onChange={(e) => setSearchQuery(e.target.value)}
                          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                          className="w-full bg-neutral-950 border border-neutral-700 rounded-lg pl-10 pr-4 py-2.5 text-white focus:outline-none focus:border-emerald-500 transition-all"
                          placeholder="Try 'coffee', 'shopping' or 'entertainment'..."
                        />
                      </div>
                      <button
                        onClick={handleSearch}
                        disabled={isSearching}
                        className="bg-emerald-600 hover:bg-emerald-500 text-white px-6 py-2 rounded-lg font-medium transition-all disabled:opacity-50"
                      >
                       {isSearching ? <RefreshCw className="w-4 h-4 animate-spin" /> : "Search"}
                      </button>
                    </div>
                  </div>

                  <div className="space-y-4">
                    {searchResults.length > 0 ? (
                      searchResults.map((res, idx) => (
                        <motion.div
                          initial={{ opacity: 0, y: 10 }}
                          animate={{ opacity: 1, y: 0 }}
                          transition={{ delay: idx * 0.1 }}
                          key={res.id}
                          className="bg-neutral-900 border border-neutral-800 p-4 rounded-xl flex items-center justify-between group hover:border-emerald-500/50 transition-all"
                        >
                          <div>
                            <h4 className="font-bold text-white uppercase text-xs tracking-wider mb-1">{res.merchant}</h4>
                            <p className="text-sm text-neutral-400">{res.description}</p>
                            <span className="text-[10px] text-neutral-600 font-mono">{new Date(res.timestamp).toLocaleString()}</span>
                          </div>
                          <div className="text-right space-y-1">
                            <div className="text-white font-mono font-bold">${res.amount.toFixed(2)}</div>
                            {(res as any).similarityScore != null && (
                              <div className="text-[10px] text-amber-400 font-mono bg-amber-500/10 px-2 py-0.5 rounded border border-amber-500/20">
                                Score: {((res as any).similarityScore).toFixed(4)}
                              </div>
                            )}
                            <div className="text-[10px] text-emerald-500 font-bold bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20">VECTOR MATCH</div>
                          </div>
                        </motion.div>
                      ))
                    ) : (
                      searchQuery && !isSearching && (
                        <div className="text-center py-12 text-neutral-600 italic">No transactions found for this semantic query.</div>
                      )
                    )}
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          {/* Right Column: Execution Audit Logs */}
          <motion.div 
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            className="bg-black border border-neutral-800 rounded-xl overflow-hidden flex flex-col h-[600px] shadow-2xl"
          >
            <div className="bg-neutral-900 border-b border-neutral-800 px-4 py-3 flex items-center gap-2">
              <Terminal className="w-4 h-4 text-neutral-400" />
              <span className="text-sm font-medium text-neutral-300 uppercase tracking-tight">Execution Audit Logs</span>
              <div className="ml-auto flex gap-1.5">
                <div className="w-2.5 h-2.5 rounded-full bg-neutral-800"></div>
                <div className="w-2.5 h-2.5 rounded-full bg-neutral-800"></div>
                <div className={`w-2.5 h-2.5 rounded-full ${isConnected ? 'bg-emerald-500 animate-pulse' : 'bg-red-500'}`}></div>
              </div>
            </div>

            <div className="flex-1 p-5 overflow-y-auto font-mono text-xs space-y-3 custom-scrollbar">
              {logs.length === 0 ? (
                <div className="text-neutral-700 italic animate-pulse">Establishing secure handshake...</div>
              ) : (
                logs.map((log, index) => (
                  <motion.div 
                    initial={{ opacity: 0, x: 5 }}
                    animate={{ opacity: 1, x: 0 }}
                    key={index} 
                    className="flex flex-col sm:flex-row gap-1 sm:gap-4 border-l border-neutral-800 pl-3 py-0.5"
                  >
                    <span className="text-neutral-600 shrink-0 text-[10px] pt-0.5">
                      {new Date(log.timestamp).toLocaleTimeString()}
                    </span>
                    <span className={`shrink-0 w-24 text-[10px] font-bold px-1.5 py-0.5 rounded leading-none flex items-center justify-center h-4 mt-0.5 ${
                      log.operation === 'ERROR' ? 'bg-red-500/20 text-red-500' :
                      log.operation === 'OBFUSCATION' || log.operation === 'UPI_PAYMENT' ? 'bg-blue-500/20 text-blue-400' :
                      log.operation === 'DATABASE' || log.operation === 'SEMANTIC_SEARCH' ? 'bg-emerald-500/20 text-emerald-400' :
                      'bg-neutral-800 text-neutral-400'
                    }`}>
                      {log.operation}
                    </span>
                    <span className="flex-1 text-neutral-300 leading-relaxed">
                      {log.message}
                    </span>
                    {log.latencyMs > 0 && (
                      <span className="text-yellow-600 shrink-0 text-[10px] pt-0.5">
                        +{log.latencyMs}ms
                      </span>
                    )}
                  </motion.div>
                ))
              )}
              <div ref={terminalEndRef} />
            </div>
          </motion.div>

        </div>
        
        {/* Footer */}
        <footer className="pt-12 pb-8 border-t border-neutral-800 text-center">
          <p className="text-xs text-neutral-500 font-medium tracking-wide flex items-center justify-center gap-2">
            <Lock className="w-3 h-3 text-emerald-500" />
            Securing your Vectors by Vector obfuscation with Orthogonal Matrix and MongoDB CSFLE
          </p>
        </footer>
      </div>
    </main>
  );
}