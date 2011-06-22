package com.alibaba.druid.sql.dialect.oracle.ast.visitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleDeleteStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectQueryBlock;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectTableReference;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleUpdateStatement;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.stat.TableStat.Column;
import com.alibaba.druid.stat.TableStat.Mode;
import com.alibaba.druid.stat.TableStat.Name;

public class OracleSchemaStatVisitor extends OracleASTVIsitorAdapter {

    private HashMap<TableStat.Name, TableStat>                    tableStats        = new HashMap<TableStat.Name, TableStat>();
    private Set<Column>                                   fields            = new HashSet<Column>();

    private final static ThreadLocal<Map<String, String>> aliasLocal        = new ThreadLocal<Map<String, String>>();
    private final static ThreadLocal<String>              currentTableLocal = new ThreadLocal<String>();
    private final static ThreadLocal<Mode>                modeLocal         = new ThreadLocal<Mode>();

    public boolean visit(SQLSubqueryTableSource x) {
        Map<String, String> aliasMap = aliasLocal.get();
        if (aliasMap != null) {
            if (x.getAlias() != null) {
                aliasMap.put(x.getAlias(), null);
            }
        }
        return true;
    }

    public boolean visit(SQLExprTableSource x) {
        if (x.getExpr() instanceof SQLIdentifierExpr) {
            String ident = ((SQLIdentifierExpr) x.getExpr()).getName();
            TableStat stat = tableStats.get(ident);
            if (stat == null) {
                stat = new TableStat();
                tableStats.put(new TableStat.Name(ident), stat);
            }

            Mode mode = modeLocal.get();
            switch (mode) {
                case Delete:
                    stat.incrementDeleteCount();
                    break;
                case Insert:
                    stat.incrementInsertCount();
                    break;
                case Update:
                    stat.incrementUpdateCount();
                    break;
                case Select:
                    stat.incrementSelectCount();
                    break;
                default:
                    break;
            }

            Map<String, String> aliasMap = aliasLocal.get();
            if (aliasMap != null) {
                if (x.getAlias() != null) {
                    aliasMap.put(x.getAlias(), ident);
                }
                aliasMap.put(ident, ident);
            }
        }

        return false;
    }

    public boolean visit(OracleSelectTableReference x) {
        if (x.getExpr() instanceof SQLIdentifierExpr) {
            String ident = ((SQLIdentifierExpr) x.getExpr()).getName();
            TableStat stat = tableStats.get(ident);
            if (stat == null) {
                stat = new TableStat();
                tableStats.put(new TableStat.Name(ident), stat);
            }

            Mode mode = modeLocal.get();
            switch (mode) {
                case Delete:
                    stat.incrementDeleteCount();
                    break;
                case Insert:
                    stat.incrementInsertCount();
                    break;
                case Update:
                    stat.incrementUpdateCount();
                    break;
                case Select:
                    stat.incrementSelectCount();
                    break;
                default:
                    break;
            }

            Map<String, String> aliasMap = aliasLocal.get();
            if (aliasMap != null) {
                if (x.getAlias() != null) {
                    aliasMap.put(x.getAlias(), ident);
                }
                aliasMap.put(ident, ident);
            }
        }

        return false;
    }

    public boolean visit(SQLSelect x) {
        return true;
    }

    public void endVisit(SQLSelect x) {
    }

    public boolean visit(SQLUpdateStatement x) {
        aliasLocal.set(new HashMap<String, String>());

        String ident = x.getTableName().toString();
        currentTableLocal.set(ident);

        TableStat stat = tableStats.get(ident);
        if (stat == null) {
            stat = new TableStat();
            tableStats.put(new TableStat.Name(ident), stat);
        }
        stat.incrementUpdateCount();

        Map<String, String> aliasMap = aliasLocal.get();
        aliasMap.put(ident, ident);

        accept(x.getItems());
        accept(x.getWhere());

        return false;
    }

    public void endVisit(SQLUpdateStatement x) {
        aliasLocal.set(null);
    }

    public boolean visit(OracleUpdateStatement x) {
        aliasLocal.set(new HashMap<String, String>());

        if (x.getTable() instanceof SQLIdentifierExpr) {
            String ident = x.getTable().toString();
            currentTableLocal.set(ident);

            TableStat stat = tableStats.get(ident);
            if (stat == null) {
                stat = new TableStat();
                tableStats.put(new TableStat.Name(ident), stat);
            }
            stat.incrementUpdateCount();

            Map<String, String> aliasMap = aliasLocal.get();
            aliasMap.put(ident, ident);
        } else {
            accept(x.getTable());
        }

        accept(x.getSetClause());
        accept(x.getWhere());

        return false;
    }

    public void endVisit(OracleUpdateStatement x) {
        aliasLocal.set(null);
    }

    public boolean visit(OracleDeleteStatement x) {
        return visit((SQLDeleteStatement) x);
    }

    public void endVisit(OracleDeleteStatement x) {
        aliasLocal.set(null);
    }

    public boolean visit(SQLDeleteStatement x) {
        aliasLocal.set(new HashMap<String, String>());

        x.putAttribute("_original_use_mode", modeLocal.get());
        modeLocal.set(Mode.Delete);

        aliasLocal.set(new HashMap<String, String>());

        String ident = ((SQLIdentifierExpr) x.getTableName()).getName();
        currentTableLocal.set(ident);

        TableStat stat = tableStats.get(ident);
        if (stat == null) {
            stat = new TableStat();
            tableStats.put(new TableStat.Name(ident), stat);
        }
        stat.incrementDeleteCount();

        accept(x.getWhere());

        return false;
    }

    public void endVisit(SQLDeleteStatement x) {
        aliasLocal.set(null);
    }

    public boolean visit(SQLSelectStatement x) {
        aliasLocal.set(new HashMap<String, String>());
        return true;
    }

    public void endVisit(SQLSelectStatement x) {
        aliasLocal.set(null);
    }

    public void endVisit(SQLInsertStatement x) {
        Mode originalMode = (Mode) x.getAttribute("_original_use_mode");
        modeLocal.set(originalMode);
    }

    public boolean visit(SQLInsertStatement x) {
        x.putAttribute("_original_use_mode", modeLocal.get());
        modeLocal.set(Mode.Insert);

        aliasLocal.set(new HashMap<String, String>());

        String originalTable = currentTableLocal.get();

        if (x.getTableName() instanceof SQLIdentifierExpr) {
            String ident = ((SQLIdentifierExpr) x.getTableName()).getName();
            currentTableLocal.set(ident);
            x.putAttribute("_old_local_", originalTable);

            TableStat stat = tableStats.get(ident);
            if (stat == null) {
                stat = new TableStat();
                tableStats.put(new TableStat.Name(ident), stat);
            }
            stat.incrementInsertCount();

            Map<String, String> aliasMap = aliasLocal.get();
            if (aliasMap != null) {
                if (x.getAlias() != null) {
                    aliasMap.put(x.getAlias(), ident);
                }
                aliasMap.put(ident, ident);
            }
        }

        accept(x.getColumns());
        accept(x.getValues());
        accept(x.getQuery());

        return false;
    }

    protected void accept(SQLObject x) {
        if (x != null) {
            x.accept(this);
        }
    }

    protected void accept(List<? extends SQLObject> nodes) {
        for (int i = 0, size = nodes.size(); i < size; ++i) {
            accept(nodes.get(i));
        }
    }

    public boolean visit(SQLSelectQueryBlock x) {

        if (x.getFrom() instanceof SQLSubqueryTableSource) {
            x.getFrom().accept(this);
            return false;
        }

        x.putAttribute("_original_use_mode", modeLocal.get());
        modeLocal.set(Mode.Select);

        String originalTable = currentTableLocal.get();

        if (x.getFrom() instanceof SQLExprTableSource) {
            SQLExprTableSource tableSource = (SQLExprTableSource) x.getFrom();
            if (tableSource.getExpr() instanceof SQLIdentifierExpr) {
                String ident = ((SQLIdentifierExpr) tableSource.getExpr()).getName();
                currentTableLocal.set(ident);
                x.putAttribute("_old_local_", originalTable);
            }
        }

        x.getFrom().accept(this); // 提前执行，获得aliasMap

        return true;
    }

    public void endVisit(SQLSelectQueryBlock x) {
        String originalTable = (String) x.getAttribute("_old_local_");
        currentTableLocal.set(originalTable);

        Mode originalMode = (Mode) x.getAttribute("_original_use_mode");
        modeLocal.set(originalMode);
    }

    public boolean visit(OracleSelectQueryBlock x) {
        return visit((SQLSelectQueryBlock) x);
    }

    public void endVisit(OracleSelectQueryBlock x) {
        endVisit((SQLSelectQueryBlock) x);
    }

    public boolean visit(SQLJoinTableSource x) {
        return true;
    }

    public boolean visit(SQLPropertyExpr x) {
        if ("ROWNUM".equalsIgnoreCase(x.getName())) {
            return false;
        }

        if (x.getOwner() instanceof SQLIdentifierExpr) {
            String owner = ((SQLIdentifierExpr) x.getOwner()).getName();

            if (owner != null) {
                Map<String, String> aliasMap = aliasLocal.get();
                if (aliasMap != null) {
                    String table = aliasMap.get(owner);

                    // table == null时是SubQuery
                    if (table != null) {
                        fields.add(new Column(table, x.getName()));
                    }
                }
            }
        }
        return false;
    }

    public boolean visit(SQLIdentifierExpr x) {
        if ("ROWNUM".equalsIgnoreCase(x.getName())) {
            return false;
        }

        String currentTable = currentTableLocal.get();

        if (currentTable != null) {
            fields.add(new Column(currentTable, x.getName()));
        }
        return false;
    }

    public Map<Name, TableStat> getTables() {
        return tableStats;
    }
    
    public boolean containsTable(String tableName) {
        return tableStats.containsKey(new TableStat.Name(tableName));
    }

    public Set<Column> getFields() {
        return fields;
    }

}
